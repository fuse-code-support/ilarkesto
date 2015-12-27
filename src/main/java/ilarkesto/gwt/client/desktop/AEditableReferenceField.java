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

import ilarkesto.core.base.EnumMapper;
import ilarkesto.core.base.Utl;
import ilarkesto.core.persistance.AEntity;
import ilarkesto.gwt.client.AGwtApplication;
import ilarkesto.gwt.client.ClientDataTransporter;
import ilarkesto.gwt.client.desktop.fields.AEditableSelectOneField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AEditableReferenceField<E extends AEntity> extends AEditableSelectOneField implements
		DataForClientLoader {

	protected abstract E getSelectedEntity();

	protected abstract void applyValue(E entity);

	public void loadDataForClientOnServer(ClientDataTransporter transporter, ActivityParameters parameters) {
		transporter.sendToClient(getSelectableEntities());
	}

	@Override
	public ActivityParameters createParametersForServer() {
		throw new IllegalStateException(getClass().getName() + ".createParametersForServer() not implemented.");
	}

	protected Collection<E> getSelectableEntities() {
		return Utl.toList(getSelectedEntity());
	}

	@Override
	public final void applyValue(String value) {
		E entity = (E) AEntity.getById(value);
		applyValue(entity);
	}

	@Override
	public EnumMapper<String, String> getOptions() {
		return new EnumMapper<String, String>() {

			@Override
			public List<String> getKeys() {
				ArrayList<String> ret = new ArrayList<String>();
				for (E entity : getSelectableEntities()) {
					ret.add(entity.getId());
				}
				return ret;
			}

			@Override
			public String getValueForKey(String key) {
				return getLabel((E) AEntity.getById(key));
			}
		};
	}

	@Override
	public final String getSelectedOptionKey() {
		E entity = getSelectedEntity();
		return entity == null ? null : entity.getId();
	}

	protected String getLabel(E selectableEntity) {
		return AGwtApplication.get().getEntityLabel(selectableEntity);
	}

	@Override
	public Widget createLabelWidget() {
		E entity = getSelectedEntity();
		if (entity == null) return super.createLabelWidget();
		if (!AGwtApplication.get().getNavigator().isEntityActivityAvailable(entity)) return super.createLabelWidget();

		Widget originalLabel = super.createLabelWidget();
		// originalLabel.getElement().getStyle().setFloat(Float.LEFT);

		Widget button = Widgets.gotoEntityButton(entity);
		button.getElement().getStyle().setFloat(Float.RIGHT);
		button.getElement().getStyle().setMarginTop(-Widgets.defaultSpacing, Unit.PX);

		FlowPanel panel = new FlowPanel();
		panel.add(originalLabel);

		panel.add(button);
		return panel;
	}

	public E getEntity(Item item) {
		return (E) AEntity.getById(item.getKey());
	}

}
