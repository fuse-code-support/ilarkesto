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
import ilarkesto.gwt.client.AAction;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;

public class ActionButton implements IsWidget {

	private AAction action;
	private Button button;
	private boolean focusable;
	private boolean forceText;
	private Integer iconSize;
	private Float iconOpacity;

	public ActionButton(AAction action, boolean focusable) {
		Args.assertNotNull(action, "action");
		this.action = action;
		this.focusable = focusable;
	}

	public ActionButton(AAction action) {
		this(action, true);
	}

	@Override
	public ButtonBase asWidget() {
		if (button == null) {
			button = new Button();
			button.setStyleName("goon-Button");
			boolean enabled = action.getExecutionVeto() == null && action.isPermitted();
			if (enabled) {
				Image icon = action.getIcon();
				if (icon == null || forceText) {
					button.setText(action.getLabel());
				} else {
					if (iconSize != null) icon.setSize(iconSize + "px", iconSize + "px");
					if (iconOpacity != null) icon.getElement().getStyle().setOpacity(iconOpacity.floatValue());
					button.getElement().appendChild(icon.getElement());
					button.addStyleDependentName("iconOnly");
					String title = action.getLabel();
					String tooltip = action.getTooltip();
					if (!Str.isBlank(tooltip)) title += ": " + tooltip;
					button.setTitle(title);
				}
			} else {
				button.addStyleDependentName("disabled");
			}
			String tooltip = action.getTooltip();
			if (!Str.isBlank(tooltip)) button.setTitle(tooltip);
			button.addClickHandler(action);
			button.getElement().setId(action.getId());
			button.setEnabled(enabled);
			if (!focusable) button.setTabIndex(-2);
		}
		return button;
	}

	public ActionButton setForceText(boolean forceText) {
		this.forceText = forceText;
		return this;
	}

	public ActionButton setIconSize(Integer iconSize) {
		this.iconSize = iconSize;
		return this;
	}

	public ActionButton setIconOpacity(float iconOpacity) {
		this.iconOpacity = iconOpacity;
		return this;
	}

	public ActionButton setWidth100() {
		asWidget().getElement().getStyle().setWidth(100, Unit.PCT);
		return this;
	}

	public Element getElement() {
		return asWidget().getElement();
	}

}
