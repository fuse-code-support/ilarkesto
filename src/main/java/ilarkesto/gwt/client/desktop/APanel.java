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

import ilarkesto.core.base.Str;
import ilarkesto.gwt.client.Updatable;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public abstract class APanel implements IsWidget, Updatable {

	private BuilderPanel panel;

	protected abstract void onUpdate(BuilderPanel panel);

	protected String getColorForMarker() {
		return null;
	}

	@Override
	public final Updatable update() {
		if (panel == null) {
			panel = new BuilderPanel();
			panel.setId(getId());
			onInit(panel);
		} else {
			if (isAutoClearOnUpdate()) clear();
		}
		if (getColorForMarker() != null) panel.addColorMarker(getColorForMarker());
		try {
			onUpdate(panel);
		} catch (Exception ex) {
			throw new RuntimeException(Str.getSimpleName(getClass()) + ".update() failed.", ex);
		}
		return this;
	}

	private BuilderPanel clear() {
		onClear();
		return panel.clear();
	}

	protected void onClear() {}

	protected void onInit(BuilderPanel panel) {
		String selfdocKey = getSelfdocKey();
		if (selfdocKey != null) panel.prepareTitleAction(Widgets.selfdocAction(selfdocKey, null, null));
	}

	protected String getSelfdocKey() {
		return getClass().getSimpleName();
	}

	protected boolean isAutoClearOnUpdate() {
		return true;
	}

	@Override
	public final Widget asWidget() {
		if (panel == null) update();
		return panel.asWidget();
	}

	protected String getId() {
		return Str.getSimpleName(getClass()).replace('$', '_');
	}

}
