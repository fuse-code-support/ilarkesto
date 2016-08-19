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
import ilarkesto.core.logging.Log;
import ilarkesto.core.persistance.Entity;
import ilarkesto.gwt.client.AGwtApplication;
import ilarkesto.gwt.client.Gwt;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

public abstract class AGwtNavigator implements ValueChangeHandler<String> {

	private static final Log log = Log.get(AGwtNavigator.class);

	private boolean disabled;

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		if (isDisabled()) return;
		String token = event.getValue();
		log.info("History token changed:", token);
		handleToken(token);
	}

	public void handleToken(String token) {
		log.debug("handleToken()", token);
		if (isDisabled()) return;
		if (Str.isBlank(token)) token = getApplicationStartToken();
		int activityNameEndIdx = token.indexOf(Gwt.HISTORY_TOKEN_SEPARATOR);
		String activityParamsToken = null;
		String activityName;
		if (activityNameEndIdx < 0) {
			activityName = token;
		} else {
			activityName = token.substring(0, activityNameEndIdx);
			activityParamsToken = token.substring(activityNameEndIdx + 1);
		}
		startActivity(activityName, activityParamsToken);
	}

	protected String getApplicationStartToken() {
		return "Home";
	}

	private void startActivity(String activityName, String activityParamsToken) {
		log.info("startActivity()", activityName, activityParamsToken);
		ActivityParameters parameters = ActivityParameters.parseToken(activityParamsToken);

		AActivity activity = AGwtApplication.get().getActivityCatalog().instantiateActivity(activityName);

		activity.startAsRoot(parameters);
	}

	protected Entity switchUiEntity(Entity entity) {
		return entity;
	}

	public String getTokenForEntityActivity(Entity entity) {
		Args.assertNotNull(entity, "entity");
		entity = getUiEntity(entity);
		return getActivityNameForEntity(entity) + new ActivityParameters(entity).createToken();
	}

	public Entity getUiEntity(Entity entity) {
		if (entity == null) return null;
		Entity uiEntity = switchUiEntity(entity);
		if (uiEntity == null) {
			log.error("Switching UI entity provided null:",
				AGwtApplication.get().getNavigator().getActivityNameForEntity(entity), entity.getId(), entity);
			return entity;
		}
		return uiEntity;
	}

	public boolean isEntityActivityAvailable(Entity entity) {
		if (entity == null) return false;
		entity = getUiEntity(entity);
		String activity = getActivityNameForEntity(entity);
		return AGwtApplication.get().getActivityCatalog().isActivityAvailable(activity);
	}

	public String getActivityNameForEntity(Entity entity) {
		return Str.getSimpleName(entity.getClass());
	}

	public void disable() {
		disabled = true;
	}

	public boolean isDisabled() {
		return disabled;
	}
}
