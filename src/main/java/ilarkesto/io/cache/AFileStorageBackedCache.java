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
package ilarkesto.io.cache;

import ilarkesto.core.base.OperationObserver;
import ilarkesto.core.base.RuntimeTracker;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.Tm;
import ilarkesto.io.AFileStorage;
import ilarkesto.io.IO;
import ilarkesto.json.JsonObject;

import java.io.File;

public abstract class AFileStorageBackedCache<T> {

	protected final Log log = Log.get(getClass());

	private AFileStorage storage;
	private AFileStorage payloadStorage;
	private JsonObject jStatus;

	private T value;
	private boolean valueLoaded;

	protected abstract T loadValueFromRemote(OperationObserver observer);

	protected abstract T loadValueFromCache(AFileStorage storage);

	protected abstract void saveValueToCache(AFileStorage storage, T value);

	protected abstract boolean isUpdateRequired();

	public AFileStorageBackedCache(AFileStorage storage) {
		super();
		this.storage = storage;
		payloadStorage = storage.getSubStorage("payload");
	}

	protected T createDefaultValue() {
		return null;
	}

	public final T getValue() {
		if (valueLoaded) return value;
		loadValueFromCache();
		return value;
	}

	private void loadValueFromCache() {
		log.debug("loadValueFromCache()");
		try {
			value = loadValueFromCache(payloadStorage);
		} catch (Exception ex) {
			value = null;
		}
		if (value == null) value = createDefaultValue();
		valueLoaded = true;
	}

	private void saveValueToCache() {
		log.debug("saveValueToCache()");
		loadStatus();
		saveValueToCache(payloadStorage, value);
		jStatus.put("lastUpdated", System.currentTimeMillis());
		saveStatus();
	}

	public final void update(OperationObserver observer) {
		RuntimeTracker rt = new RuntimeTracker();
		log.debug("update()");
		observer.onOperationInfoChanged(OperationObserver.UPDATING);
		loadStatus();
		value = loadValueFromRemote(observer);
		saveValueToCache();
		log.info("Updated in", rt.getRuntimeFormated());
	}

	public final void updateIfRequired(OperationObserver observer) {
		loadStatus();
		if (!isUpdateRequired()) {
			log.info("Update not required. Skipping.");
			return;
		}
		update(observer);
	}

	public boolean isLastUpdatedLongerAgoThen(long millis) {
		Long lastUpdated = getTimeLastUpdated();
		if (lastUpdated == null) return false;
		return Tm.getCurrentTimeMillis() - lastUpdated.longValue() > millis;
	}

	public Long getTimeLastUpdated() {
		loadStatus();
		return jStatus.getLong("lastUpdated");
	}

	private void saveStatus() {
		IO.writeFile(getStatusFile(), jStatus.toFormatedString(), IO.UTF_8);
	}

	private void loadStatus() {
		if (jStatus != null) return; // already loaded
		File statusFile = getStatusFile();
		if (!statusFile.exists()) {
			jStatus = new JsonObject();
			return;
		}
		jStatus = new JsonObject(IO.readFile(statusFile));
	}

	private File getStatusFile() {
		return storage.getFile("cache-status.json");
	}

}
