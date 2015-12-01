/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ilarkesto.gwt.client;

import ilarkesto.core.base.RuntimeTracker;
import ilarkesto.core.base.Str;
import ilarkesto.core.base.Utl;
import ilarkesto.core.logging.Log;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AWidget extends Composite implements Updatable {

	protected Log log = Log.get(getClass());

	private boolean initializing;
	private boolean initialized;
	private Wrapper wrapper;

	private String updateSignature;
	private String resetSignature;

	protected abstract Widget onInitialization();

	public AWidget() {
		wrapper = new Wrapper();
		if (!GWT.isScript())
			wrapper.setContent(Gwt.createBugMarker(getClass().getName() + " is not initialized. -> call update()"));
		initWidget(wrapper);
	}

	protected String getResetSignature() {
		return null;
	}

	protected boolean isResetRequired() {
		String newSignature = getResetSignature();
		boolean resetRequired = false;
		if (!Utl.equals(newSignature, resetSignature)) resetRequired = true;
		resetSignature = newSignature;
		return resetRequired;
	}

	protected void onUpdate() {
		Element element = wrapper.getElement();
		String newId = getId();
		if (element.getId() != newId) element.setId(newId);
		Gwt.update(wrapper.getWidget());
	}

	/**
	 * Initializes the widget if not already initialized.
	 */
	public final void initialize() {

		// check if already initialized
		if (initialized) return;

		// check if initializing and prevent endless loop
		if (initializing) throw new RuntimeException("Widget already initializing: " + toString());
		initializing = true;

		// GwtLogger.DEBUG("Initializing widget: " + toString());
		Widget content = onInitialization();
		wrapper.setContent(content);
		wrapper.getElement().setId(getId());

		resetSignature = getResetSignature();

		initialized = true;
		initializing = false;

	}

	public final void reset() {
		log.debug("reset()");
		initialized = false;
	}

	protected void replaceContent(Widget widget) {
		initialize();
		wrapper.setContent(widget);
	}

	@Override
	public final AWidget update() {
		RuntimeTracker rt = new RuntimeTracker();
		AWidget ret = updateInternal();
		if (rt.getRuntime() > 500) Log.get(getClass()).warn("Long update time:", rt.getRuntimeFormated());
		return ret;
	}

	private AWidget updateInternal() {
		if (isResetRequired()) reset();
		initialize();
		if (!isUpdateRequired()) return this;
		onUpdate();
		return this;
	}

	protected String getUpdateSignature() {
		return null;
	}

	protected boolean isUpdateRequired() {
		String newSignature = getUpdateSignature();
		boolean updateRequired = false;
		if (newSignature == null || updateSignature == null || !Utl.equals(newSignature, updateSignature))
			updateRequired = true;
		updateSignature = newSignature;
		return updateRequired;
	}

	public final boolean isInitialized() {
		return initialized;
	}

	protected final void setHeight100() {
		wrapper.setStyleName("AWidget-height100");
	}

	public String getId() {
		return Str.getSimpleName(getClass()).replace('$', '_');
	}

	@Override
	public String toString() {
		return Gwt.getSimpleName(getClass());
	}

	@Override
	public void setStyleName(String style) {
		wrapper.setStyleName(style);
	}

	private class Wrapper extends SimplePanel {

		// private Widget content;
		//
		// @Override
		// protected Widget createWidget() {
		// initialize();
		// return content;
		// }
		//
		public void setContent(Widget conent) {
			// this.content = conent;
			setWidget(conent);
		}

	}

}
