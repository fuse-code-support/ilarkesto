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
package ilarkesto.persistence.file;

import ilarkesto.core.base.Utl;
import ilarkesto.core.logging.Log;
import ilarkesto.core.persistance.AEntity;
import ilarkesto.core.time.Date;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.core.time.DateRange;
import ilarkesto.io.IO;
import ilarkesto.json.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AEntityJsonFileUpgrades {

	protected final Log log = Log.get(getClass());

	private List<FileUpgrader> upgraders = new ArrayList<FileUpgrader>();

	protected abstract void initialize();

	public AEntityJsonFileUpgrades() {
		initialize();
	}

	protected boolean convertDateAndTimeToDate(JsonObject json, String... properties) {
		boolean converted = false;
		for (String property : properties) {
			String value = json.getString(property);
			if (value == null) continue;
			try {
				new Date(value);
			} catch (IllegalArgumentException ex) {
				String newValue = new DateAndTime(value).getDate().toString();
				json.put(property, newValue);
				converted = true;
			}
		}
		return converted;
	}

	protected boolean convertToDateRange(JsonObject json, String oldStartProperty, String oldEndProperty,
			String newRangeProperty) {
		if (!json.contains(oldStartProperty) && !json.contains(oldEndProperty)) return false;

		String sStart = json.getString(oldStartProperty);
		Date start = sStart == null ? Date.today() : new Date(sStart);
		String sEnd = json.getString(oldEndProperty);
		Date end = sEnd == null ? Date.today() : new Date(sEnd);
		DateRange dateRange;
		try {
			dateRange = new DateRange(start, end);
		} catch (Exception ex) {
			log.error("DateRange-Upgrade mit ungültigem Zeitraum:", json.toFormatedString());
			dateRange = new DateRange(start);
		}

		json.put(newRangeProperty, dateRange.toString());
		json.remove(oldStartProperty);
		json.remove(oldEndProperty);

		return true;
	}

	protected boolean rename(JsonObject json, String fromFieldname, String toFieldname) {
		if (json.contains(fromFieldname)) {
			Object value = json.get(fromFieldname);
			json.remove(fromFieldname);
			json.put(toFieldname, value);
			return true;
		}
		return false;
	}

	protected boolean addIfMissing(JsonObject json, String fieldName, boolean value) {
		if (json.isSet(fieldName)) return false;
		json.put(fieldName, value);
		return true;
	}

	protected boolean addNew(JsonObject json, String fieldName, boolean value) {
		if (json.contains(fieldName)) return false;
		json.put(fieldName, value);
		return true;
	}

	protected boolean remove(JsonObject json, String... fields) {
		boolean removed = false;
		for (String field : fields) {
			if (json.contains(field)) {
				json.remove(field);
				removed = true;
			}
		}
		return removed;
	}

	public int getSoftwareVersion() {
		int version = 0;
		for (FileUpgrader upgrader : upgraders) {
			version = Math.max(version, upgrader.getChangeInsertionVersion());
		}
		return version;
	}

	public void upgradeEntitiesDir(File dir, int fileVersion) {
		Collections.sort(upgraders);

		if (fileVersion < 18) {
			File mediaplanStatuss = new File(dir.getPath() + "/MediaplanStatus");
			if (mediaplanStatuss.exists()) IO.delete(mediaplanStatuss);
		}
		if (fileVersion < 5) {
			File werbetraegerinfos = new File(dir.getPath() + "/Werbetraegerinfo");
			if (werbetraegerinfos.exists()) IO.move(werbetraegerinfos, new File(dir.getPath() + "/Werbetraeger"));
		}
	}

	public void upgradeEntity(File file, Class<? extends AEntity> entityType, int fileVersion) {
		for (FileUpgrader upgrader : upgraders) {
			Class upgraderType = upgrader.getEntityType();
			if (fileVersion < upgrader.getChangeInsertionVersion()
					&& (upgraderType == null || entityType.equals(upgraderType)))
				upgrader.upgrade(file, fileVersion);

		}
	}

	public abstract class JsonUpgrader<E extends AEntity> extends FileUpgrader<E> {

		protected File entityFile;

		public JsonUpgrader(Class<E> entityType, int changeInsertionVersion) {
			super(entityType, changeInsertionVersion);
		}

		@Override
		public final void upgrade(File file, int fileVersion) {
			this.entityFile = file;
			JsonObject json = JsonObject.loadFile(file, false);
			boolean changed;
			try {
				changed = upgrade(json);
			} catch (Exception ex) {
				throw new RuntimeException("Upgrading JSON file failed: " + file, ex);
			}
			if (!changed) return;
			log.info("Entity JSON file upgraded:", file);
			json.save();
		}

		protected abstract boolean upgrade(JsonObject json);

		protected final boolean upgradeTimeAndUser(JsonObject json, String property) {
			String refPropertyName = property + "Id";
			String userAndTimeId = json.getString(refPropertyName);
			if (userAndTimeId == null) return remove(json, refPropertyName);

			File userAndTimeFile = new File(
					entityFile.getParentFile().getParentFile().getPath() + "/UserAndTime/" + userAndTimeId + ".json");

			JsonObject userAndTimeJson = JsonObject.loadFile(userAndTimeFile, false);

			json.put(property, userAndTimeJson.getString("userId") + " " + userAndTimeJson.getString("time"));

			remove(json, refPropertyName);
			return true;
		}

	}

	public abstract class FileUpgrader<E extends AEntity> implements Comparable<FileUpgrader<E>> {

		private Class<E> entityType;
		private int changeInsertionVersion;

		public FileUpgrader(Class<E> entityType, int changeInsertionVersion) {
			super();
			this.entityType = entityType;
			this.changeInsertionVersion = changeInsertionVersion;
			upgraders.add(this);
		}

		public abstract void upgrade(File file, int fileVersion);

		public Class<E> getEntityType() {
			return entityType;
		}

		public final int getChangeInsertionVersion() {
			return changeInsertionVersion;
		}

		@Override
		public final int compareTo(FileUpgrader<E> o) {
			return Utl.compare(changeInsertionVersion, o.changeInsertionVersion);
		}

	}

}
