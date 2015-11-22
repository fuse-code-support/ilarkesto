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
package ilarkesto.gwt.client.desktop;

import ilarkesto.core.base.Args;
import ilarkesto.core.base.Str;
import ilarkesto.core.persistance.AEntity;
import ilarkesto.core.persistance.Entity;
import ilarkesto.core.time.DateRange;
import ilarkesto.gwt.client.Gwt;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ActivityParameters implements Serializable, IsSerializable {

	private Map<String, String> params = new HashMap<String, String>();

	public static ActivityParameters parseToken(String token) {
		ActivityParameters params = new ActivityParameters();
		if (token == null) return params;
		token = token.trim();
		if (token.isEmpty()) return params;
		int fromIdx = 0;
		int tokenLength = token.length();
		while (fromIdx < tokenLength) {
			int eqIdx = token.indexOf('=', fromIdx);
			if (eqIdx < 0) {
				params.put("value", token);
			}
			int endIdx = token.indexOf(Gwt.HISTORY_TOKEN_SEPARATOR, eqIdx + 1);
			if (endIdx < 0) endIdx = tokenLength;
			String key = token.substring(fromIdx, eqIdx);
			String value = token.substring(eqIdx + 1, endIdx);
			params.put(key, value);
			fromIdx = endIdx + 1;
		}
		return params;
	}

	public ActivityParameters() {}

	public ActivityParameters(String value) {
		Args.assertNotNull(value, "value");
		put("value", value);
	}

	public ActivityParameters(Entity entity) {
		this(entity.getId());
	}

	public ActivityParameters put(String name, String value) {
		Args.assertNotNull(name, "name");
		params.put(name, value);
		return this;
	}

	public String get(String name) {
		return params.get(name);
	}

	public String getMandatory(String name) {
		String value = get(name);
		if (value == null)
			throw new IllegalStateException("Missing mandatory activity parameter: " + name + ". -> "
					+ Str.format(params));
		return value;
	}

	public String createToken() {
		if (params.size() == 1 && params.containsKey("value"))
			return Gwt.HISTORY_TOKEN_SEPARATOR + params.values().iterator().next();
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			String value = entry.getValue();
			if (value == null) continue;
			sb.append(Gwt.HISTORY_TOKEN_SEPARATOR);
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(value);
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return createToken();
	}

	// --- helpers ---

	public ActivityParameters put(Entity entity) {
		return put(Str.lowercaseFirstLetter(Str.getSimpleName(entity.getClass())) + "Id", entity.getId());
	}

	public ActivityParameters put(String name, boolean value) {
		return put(name, String.valueOf(value));
	}

	public ActivityParameters put(String name, Object value) {
		if (value == null) return put(name, null);
		if (value instanceof Entity) return put(name, ((Entity) value).getId());
		return put(name, value.toString());
	}

	public boolean getBoolean(String name, boolean defaultValue) {
		String value = get(name);
		if (value == null) return defaultValue;
		return Str.isTrue(value);
	}

	public DateRange getDateRange(String name) {
		String value = get(name);
		if (value == null) return null;
		return new DateRange(value);
	}

	public <E extends AEntity> E getEntity(String name, Class<E> type) {
		String id = get(name);
		return (E) AEntity.getById(id);
	}

	public <E extends AEntity> E getMandatoryEntity(String name, Class<E> type) {
		String id = getMandatory(name);
		return (E) AEntity.getById(id);
	}

	public <E extends AEntity> E getEntity(Class<E> type) {
		return getEntity(Str.lowercaseFirstLetter(Str.getSimpleName(type)) + "Id", type);
	}

	public <E extends AEntity> E getMandatoryEntity(Class<E> type) {
		return getMandatoryEntity(Str.lowercaseFirstLetter(Str.getSimpleName(type)) + "Id", type);
	}

	public String getMandatoryValue() {
		return getMandatory("value");
	}

	public String getValue() {
		return get("value");
	}

	public <E extends Entity> E getMandatoryValueAsEntity(Class<E> type) {
		String id = getMandatory("value");
		return (E) AEntity.getById(id);
	}

	public List<String> getList(String name) {
		return Str.parseCommaSeparatedString(get(name));
	}

}
