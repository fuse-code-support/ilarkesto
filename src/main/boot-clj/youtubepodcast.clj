#!/usr/bin/env boot

(set-env! :dependencies '[[org.clojure/clojure "1.9.0"]
                          [cheshire "5.8.0"]])

(require '[cheshire.core :as json])
(require '[clojure.java.shell :as shell])
(require '[clojure.java.io :as io])
(require '[clojure.xml :as xml])
(require '[clojure.edn :as edn])

(def youtube-api-key (get-in (edn/read-string (slurp "/etc/youtube-podcast.edn")) [:youtube :api-key]))
(def youtube-channel-id "PL-cdM2c5tq3Inccp1d28lWWh3Jfdo9FrS")

(def youtube-channel-max-results 50)
(def youtube-channel-url (str "https://www.googleapis.com/youtube/v3/playlistItems"
                              "?part=contentDetails,snippet"
                              "&maxResults=" youtube-channel-max-results
                              "&key=" youtube-api-key
                              "&playlistId=" youtube-channel-id))


(defn load-channel-videos []
  (println "Loading Channel...")
  (-> youtube-channel-url
      slurp
      (json/parse-string true)
      :items))
      ;(->> (mapv :contentDetails))))

(defn video-id->url [video-id]
  (str "https://www.youtube.com/watch?v=" video-id))

(defn download-video [video-id]
  (println "  -> Downloading video...")
  (let [ret (shell/sh "youtube-dl"
                      "--id"
                      "--no-progress"
                      "--continue"
                      "--no-mtime"
                      "--write-thumbnail"
                      "--extract-audio"
                      "--audio-format" "mp3"
                      (video-id->url video-id))]
    (if-not (= 0 (:exit ret))
      (throw (ex-info "youtube-dl failed" {:ret ret :video-id video-id})))))

(defn download-missing-files [items]
  (doall (for [item items]
           (let [video-id (get-in item [:contentDetails :videoId])
                 file (io/as-file (str video-id ".mp3"))
                 file-exists (.exists file)]
             (println "\n" (get-in item [:snippet :title]))
             (if-not file-exists (download-video video-id)))))
  items)

(defn ->ascii [s]
  (if s
    (.trim
     (apply str (filter #(and
                          (not (= 38 (int %)))
                          (or
                           (<= 32 (int %) 126)
                           (= 10 (int %))))
                        s)))))

(defn create-rss-item [item]
  (let [title (->ascii (get-in item [:snippet :title]))
        description (->ascii (get-in item [:snippet :description]))
        video-id (get-in item [:contentDetails :videoId])]
    {:tag :item
     :content [{:tag :title :content [title]}
               {:tag :description :content [description]}
               {:tag :guid :content [(str "youtubepodcast-" video-id)]}
               {:tag :enclosure :attrs {:url (str "http://servisto.de/youtube-podcast/resrever/"
                                                  video-id
                                                  ".mp3")}}
               {:tag "itunes:image" :attrs {:href (str "http://servisto.de/youtube-podcast/resrever/"
                                                        video-id
                                                        ".jpg")}}]}))

(defn create-rss [items]
  (with-out-str
    (xml/emit {:tag :rss
               :attrs {:version "2.0"
                       "xmlns:itunes" "http://www.itunes.com/dtds/podcast-1.0.dtd"}
               :content [{:tag :channel
                          :content (into [{:tag :title :content ["Youtube Podcast"]}
                                          {:tag :description :content ["Witek's Youtube Podcast"]}
                                          {:tag :image :content [{:tag :url :content ["https://www.chirbit.com/images/learn-more-youtube-to-audio.png"]}]}]
                                         (map create-rss-item items))}]})))

(defn write-feed [items]
  (println "Writing RSS Feed...")
  (spit "feed.rss" (create-rss items)))

(defn load! []
  (-> (load-channel-videos)
      download-missing-files
      write-feed))

;(println (->ascii "hallo & welt\nx"))
(load!)
