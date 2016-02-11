/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package ilarkesto.integration.kunagi;

import ilarkesto.core.base.OperationObserver;
import ilarkesto.core.logging.Log;
import ilarkesto.net.httpclient.HttpSession;

import java.util.HashMap;
import java.util.Map;

public class KunagiConnector {

	public static final String PARAM_SPAM_PREVENTION_CODE = "spamPreventionCode";
	public static final String SPAM_PREVENTION_CODE = "no-spam";
	public static final String SPAM_PREVENTION_TEXT = "\n\nthis-is-not-spam";

	private static Log log = Log.get(KunagiConnector.class);

	private String kunagiUrl;
	private String projectId;
	private HttpSession http = new HttpSession();

	public KunagiConnector(String kunagiUrl, String projectId) {
		super();
		this.kunagiUrl = kunagiUrl;
		this.projectId = projectId;
	}

	public KunagiConnector(String projectId) {
		this("https://servisto.de/kunagi/", projectId);
	}

	public String postIssue(OperationObserver observer, String name, String email, String subject, String text,
			String additionalInfo, boolean wiki, boolean publish) {
		String url = kunagiUrl + "submitIssue";
		observer.onOperationInfoChanged(OperationObserver.DOWNLOADING, url);
		Map<String, String> params = new HashMap<String, String>();
		params.put("projectId", projectId);
		params.put("name", name);
		params.put("email", email);
		params.put("publish", String.valueOf(publish));
		params.put("wiki", String.valueOf(wiki));
		params.put("subject", subject);
		params.put("text", text + SPAM_PREVENTION_TEXT);
		params.put("additionalInfo", additionalInfo);
		params.put(PARAM_SPAM_PREVENTION_CODE, SPAM_PREVENTION_CODE);
		String ret = http.postAndDownloadText(url, params);
		if (ret.contains("Submitting issue failed") || ret.contains("Submitting your feedback failed")) {
			log.error("Submitting issue failed:", ret, "\n", name, email, subject, text);
			throw new RuntimeException(ret);
		}
		log.info("Issue submitted:", ret);
		return ret;
	}

	public String postIssueAndGetIssReference(OperationObserver observer, String name, String email, String subject,
			String text, String additionalInfo, boolean wiki, boolean publish) {
		String responnse = postIssue(observer, name, email, subject, text, additionalInfo, wiki, publish);
		if (responnse == null) return null;
		log.info("Response:", responnse);
		int idx = responnse.indexOf("href='iss");
		if (idx < 0) return null;
		return responnse.substring(idx + 6, responnse.indexOf(".html"));
	}

}
