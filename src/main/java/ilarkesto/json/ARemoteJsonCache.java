package ilarkesto.json;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ilarkesto.core.base.Args;
import ilarkesto.core.base.OperationObserver;
import ilarkesto.core.base.RuntimeTracker;
import ilarkesto.core.logging.Log;
import ilarkesto.io.IO;

public abstract class ARemoteJsonCache<P extends AJsonWrapper> {

	protected Log log = Log.get(getClass());

	/**
	 * @param payload the current payload or null
	 * @param forced
	 * @param invalidated
	 * @return new payload or null if not updated
	 */
	protected abstract P onUpdate(P payload, boolean forced, boolean invalidated, OperationObserver observer);

	private static Map<String, Object> lockByFilepath = new HashMap<String, Object>();

	private Class<P> payloadType;
	private File file;
	private File invalidMarkerFile;

	private JsonObject wrapper;

	public ARemoteJsonCache(Class<P> payloadType, File file) {
		Args.assertNotNull(payloadType, "payloadType", file, "file");
		this.payloadType = payloadType;
		this.file = file;
	}

	protected boolean isPotentialUpdateAvailable() {
		return true;
	}

	private JsonObject getJson() {
		synchronized (getLock()) {
			if (wrapper == null) {
				log.info("Loading cache:", file);
				wrapper = JsonObject.loadFile(file, true);
			}
			return wrapper;
		}
	}

	public boolean isPayloadAvailableNow() {
		if (wrapper == null) return false;
		return wrapper.contains("payload");
	}

	public P getPayload() {
		synchronized (getLock()) {
			JsonObject json = getJson();
			P payload = AJsonWrapper.createWrapper(json.getObject("payload"), payloadType);
			if (payload == null) {
				log.info("Creating initial payload");
				payload = createInitialPayload();
				if (payload != null) {
					log.info("Initial payload created");
					json.put("payload", payload.json);
				}
			}
			return payload;
		}
	}

	public void setPayload(P payload) {
		getJson().put("payload", payload);
		save();
	}

	protected P createInitialPayload() {
		return null;
	}

	// public P getPayload_ButUpdateIfNull() throws RemoteUpdateFailedException {
	// return getPayload_ButUpdateIfNull(null);
	// }
	//
	// public P getPayload_ButUpdateIfNull(OperationObserver observer) throws RemoteUpdateFailedException {
	// if (observer == null) observer = OperationObserver.DUMMY;
	// observer.onOperationInfoChanged(OperationObserver.LOADING_CACHE);
	// synchronized (getLock()) {
	// P payload = getPayload();
	// if (payload == null) {
	// log.info("Payload does not exist, needs update");
	// update(true, observer);
	// return getPayload();
	// }
	// if (isInvalidated()) {
	// log.info("Payload is invalidated, needs update");
	// update(true, observer);
	// return getPayload();
	// }
	// return payload;
	// }
	// }

	public void update(boolean force) throws RemoteUpdateFailedException {
		update(force, null);
	}

	public void update(boolean force, OperationObserver observer) throws RemoteUpdateFailedException {
		if (observer == null) observer = OperationObserver.DUMMY;
		log.info("Request for payload update");
		if (!force && !isInvalidated() && !isPotentialUpdateAvailable()) {
			log.info("- Update for payload not required.");
			return;
		}
		synchronized (getLock()) {
			P payload = getPayload();
			log.info("Updating payload", force ? "(forced)" : "");
			observer.onOperationInfoChanged(OperationObserver.UPDATING);
			RuntimeTracker rt = new RuntimeTracker();
			try {
				payload = onUpdate(payload, force, isInvalidated(), observer);
			} catch (Exception ex) {
				throw new RemoteUpdateFailedException("Updating payload failed.", ex);
			}
			if (payload == null) {
				// payload not updated (perhaps not due)
				log.info("Payload not updated after", rt.getRuntimeFormated());
				if (!force) return;
				throw new RemoteUpdateFailedException("Loading payload failed.");
			}
			log.info("Payload updated in", rt.getRuntimeFormated());
			JsonObject json = getJson();
			json.put("payload", payload);
			save();
			IO.delete(getInvalidMarkerFile());
		}
	}

	public void save() {
		synchronized (getLock()) {
			if (wrapper != null) {
				log.info("Saving");
				long start = System.currentTimeMillis();
				wrapper.write(file, false);
				long time = System.currentTimeMillis() - start;
				log.info("Saved in", time, "ms.:", file);
			}
		}
		onSaved();
	}

	private Object getLock() {
		Object lock = lockByFilepath.get(file.getAbsolutePath());
		if (lock == null) {
			lock = file.getAbsolutePath();
			lockByFilepath.put(file.getAbsolutePath(), lock);
		}
		return lock;
	}

	protected void onSaved() {}

	public void delete() {
		synchronized (getLock()) {
			log.info("Deleting");
			wrapper = null;
			file.delete();
		}
	}

	public void unload() {
		synchronized (getLock()) {
			wrapper = null;
		}
	}

	public void invalidatePayload() {
		file.setLastModified(0);
		IO.writeFile(getInvalidMarkerFile(), "invalid", IO.UTF_8);
		log.info("Invalidated payload:", this);
	}

	private File getInvalidMarkerFile() {
		if (invalidMarkerFile == null) {
			invalidMarkerFile = new File(file.getPath() + ".invalid");
		}
		return invalidMarkerFile;
	}

	public File getFile() {
		return file;
	}

	public boolean isInvalidated() {
		if (file == null) return false;
		return getInvalidMarkerFile().exists();
	}

	public long getLastUpdated() {
		if (file == null) return 0;
		return file.lastModified();
	}

	public long getTimeSinceLastUpdated() {
		return System.currentTimeMillis() - getLastUpdated();
	}

	protected boolean isSkipUpdateByHours(int maxAgeInHours, boolean forced, boolean invalidated) {
		if (forced || invalidated) return false;
		return getHoursSinceLastUpdated() < maxAgeInHours;
	}

	protected boolean isSkipUpdateByDays(int maxAgeInDays, boolean forced, boolean invalidated) {
		return isSkipUpdateByHours(maxAgeInDays * 24, forced, invalidated);
	}

	public long getHoursSinceLastUpdated() {
		return getTimeSinceLastUpdated() / 3600000l;
	}

	public long getDaysSinceLastUpdated() {
		return getTimeSinceLastUpdated() / 86400000l;
	}

}
