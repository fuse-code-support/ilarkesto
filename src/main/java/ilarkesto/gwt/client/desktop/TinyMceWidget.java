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
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.TextArea;

public class TinyMceWidget extends TextArea {

	private static int count = 0;

	private final String id = "tinymce_" + ++count;
	private final String initialValue;

	public TinyMceWidget(final String value, final TinyMceConfigurator configurator) {
		this.initialValue = value;

		setValue(value);

		getElement().setId(id);
		setStyleName("TinyMceWidget");

		addAttachHandler(new AttachEvent.Handler() {

			@Override
			public void onAttachOrDetach(AttachEvent ev) {
				if (ev.isAttached()) {
					JSONObject conf = createConf();
					if (configurator != null) configurator.configureTinyMce(conf);
					createTinyMce(conf.getJavaScriptObject());
				}
			}

		});
	}

	private JSONObject createConf() {
		JSONObject conf = new JSONObject();
		conf.put("selector", new JSONString("#" + id));
		conf.put("auto_focus", new JSONString(id));

		conf.put("plugins", new JSONString(
				"code lists image link textcolor colorpicker media table charmap fullscreen hr nonbreaking searchreplace visualblocks visualchars wordcount"));

		conf.put("toolbar", new JSONString(
				"undo redo | styleselect | bold italic forecolor | bullist outdent indent | link image image_list"));

		conf.put("branding", JSONBoolean.getInstance(false));

		// link
		conf.put("default_link_target", new JSONString("_blank"));
		conf.put("link_assume_external_targets", JSONBoolean.getInstance(true));

		// image
		conf.put("image_advtab", JSONBoolean.getInstance(true));

		// code
		conf.put("code_dialog_width", new JSONNumber(1024));

		return conf;
	}

	private native void createTinyMce(JavaScriptObject options)
	/*-{
		$wnd.tinymce.init(options);
	}-*/;

	@Override
	public String getValue() {
		return getTinyMceContent(id);
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
		setTinyMceContent(id, value);
	}

	private native String getTinyMceContent(String id)
	/*-{
	 	return $wnd.tinyMCE.get(id).getContent();
	 }-*/;

	private native String setTinyMceContent(String id, String value)
	/*-{
	    editor = $wnd.tinyMCE.get(id);
	 	if (editor) editor.setContent(value);
	 }-*/;

	public static interface TinyMceConfigurator {

		void configureTinyMce(JSONObject conf);

	}

}
