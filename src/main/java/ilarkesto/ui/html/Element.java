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
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ilarkesto.ui.html;

import ilarkesto.base.Str;
import ilarkesto.ui.web.HtmlBuilder;
import ilarkesto.ui.web.HtmlBuilder.Tag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Element implements Component {

	protected HtmlBuilder html;

	private String name;
	private List<Object> children;
	private Map<String, String> attributes;

	public Element(String name) {
		super();
		this.name = name;
	}

	public Element add(Object... children) {
		if (children == null || children.length == 0) return this;
		if (this.children == null) this.children = new ArrayList<Object>();
		for (Object child : children) {
			if (child == null) continue;
			this.children.add(child);
		}
		return this;
	}

	public Element html(String code) {
		return add(new HtmlCodeComponent(code));
	}

	@Override
	public final void build(HtmlBuilder html) {
		this.html = html;
		Tag tag = html.startTag(name);
		if (attributes != null) {
			for (Map.Entry<String, String> attribute : attributes.entrySet()) {
				tag.set(attribute.getKey(), attribute.getValue());
			}
		}
		buildContent();
		html.endTag(name);
	}

	protected void buildContent() {
		if (children == null) return;
		for (Object child : children) {
			if (child instanceof Component) {
				((Component) child).build(html);
			} else {
				html.text(child);
			}
		}
	}

	public Element classes(String... classNames) {
		if (attributes != null && attributes.containsKey("class")) {
			attributes.put("class", attributes.get("class") + " " + Str.concat(classNames, " "));
		}
		return attr("class", Str.concat(classNames, " "));
	}

	public Element styles(String... styles) {
		if (attributes != null && attributes.containsKey("style")) {
			attributes.put("style", attributes.get("style") + " " + Str.concat(styles, "; "));
		}
		return attr("style", Str.concat(styles, "; "));
	}

	public Element attr(String name, String value) {
		if (attributes == null) attributes = new LinkedHashMap<String, String>();
		attributes.put(name, value);
		return this;
	}

	public Element id(String id) {
		return attr("id", id);
	}

}