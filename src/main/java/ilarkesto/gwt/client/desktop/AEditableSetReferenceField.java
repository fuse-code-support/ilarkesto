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

import ilarkesto.core.persistance.AEntity;
import ilarkesto.gwt.client.AGwtApplication;
import ilarkesto.gwt.client.ClientDataTransporter;
import ilarkesto.gwt.client.desktop.fields.AEditableSelectManyField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public abstract class AEditableSetReferenceField<E extends AEntity> extends AEditableSelectManyField
		implements DataForClientLoader {

	protected abstract Collection<E> getSelectableEntities();

	protected abstract Collection<E> getSelectedEntities();

	protected abstract void applyValue(Collection<E> entities);

	@Override
	public ActivityParameters createParametersForServer() {
		return new ActivityParameters();
	}

	@Override
	public void applyValue(List<String> selectedKeys) {
		List<E> selectedEntities = new ArrayList<E>(selectedKeys.size());
		for (String entityId : selectedKeys) {
			E entity = (E) AEntity.getById(entityId);
			selectedEntities.add(entity);
		}
		applyValue(selectedEntities);
	}

	@Override
	public Collection<String> createOptionKeys() {
		List<String> ret = new ArrayList<String>();
		for (E entity : getSelectableEntities()) {
			ret.add(entity.getId());
		}
		return ret;
	}

	@Override
	public String getValueForKey(String key) {
		return createLabel((E) AEntity.getById(key));
	}

	protected String createLabel(E entity) {
		return entity.toString();
	}

	@Override
	public Collection<String> getSelectedOptionKeys() {
		List<String> ret = new ArrayList<String>();
		for (E entity : getSelectedEntities()) {
			ret.add(entity.getId());
		}
		return ret;
	}

	@Override
	public void loadDataForClientOnServer(ClientDataTransporter transporter, ActivityParameters parameters) {
		transporter.sendToClient(getSelectableEntities());
		transporter.sendToClient(getSelectedEntities());
	}

	public E getEntity(Item item) {
		return (E) AEntity.getById(item.getKey());
	}

	@Override
	public IsWidget createDisplayWidget() {
		BuilderPanel bp = new BuilderPanel();

		for (String key : getSelectedOptionKeys()) {
			String itemText = getDisplayValueForKey(key);
			E entity = (E) AEntity.getById(key);

			if (!AGwtApplication.get().getNavigator().isEntityActivityAvailable(entity))
				return super.createDisplayWidget();

			Widget button = Widgets.gotoEntityButton(entity);
			button.getElement().getStyle().setFloat(com.google.gwt.dom.client.Style.Float.RIGHT);
			// button.getElement().getStyle().setMarginTop(Widgets.defaultSpacing, Unit.PX);

			HTML spacer = Widgets.html("<span style='font-size: 1px; line-height: 1px;'>&nbsp;<span>");
			spacer.getElement().getStyle().setHeight(1, Unit.PX);
			bp.createHorizontalChild().add(itemText, Widgets.flowPanel(spacer, button));
		}

		return bp;
	}

}
