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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.SimplePanel;

public class CodemirrorWidget extends SimplePanel {

	private static int count = 0;

	private final String id = "codemirror_" + ++count;
	private final boolean readonly;
	private final String initialValue;
	private JavaScriptObject codemirror;

	public CodemirrorWidget(final String mode, final String value, final boolean readonly) {
		this.initialValue = value;
		this.readonly = readonly;

		setStyleName("CodeMirrorWidget");
		getElement().setId(id);
		addAttachHandler(new AttachEvent.Handler() {

			@Override
			public void onAttachOrDetach(AttachEvent ev) {
				if (ev.isAttached()) {
					Element element = CodemirrorWidget.this.getElement();
					element.getStyle().setWidth(element.getClientWidth(), Unit.PX);
					codemirror = createCodemirror(id, mode, value == null ? "" : value, readonly);
				}
			}
		});
	}

	public String getValue() {
		if (readonly || codemirror == null) return initialValue;
		return getValue(codemirror);
	}

	private native JavaScriptObject createCodemirror(String wrapperId, String mode, String value, boolean readonly)
	/*-{
	    var wrapper = $wnd.document.getElementById(wrapperId);
		var codemirror = $wnd.CodeMirror(wrapper, {
			value: value,
			mode:  mode,
			readonly: readonly
		});
		return codemirror;
	}-*/;

	private native String getValue(JavaScriptObject codemirror)
	/*-{
		return codemirror.getValue();
	}-*/;

}
