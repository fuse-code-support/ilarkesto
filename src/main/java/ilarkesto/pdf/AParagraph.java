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
package ilarkesto.pdf;

import ilarkesto.core.base.Args;
import ilarkesto.core.base.Color;
import ilarkesto.core.base.Str;
import ilarkesto.core.html.Html;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AParagraph extends APdfElement {

	public enum Align {
		LEFT, CENTER, RIGHT
	}

	private FontStyle defaultFontStyle;
	protected float height;
	protected Align align;
	protected float spacingTop;
	protected float spacingBottom;
	protected List<AParagraphElement> elements = new ArrayList<AParagraphElement>(1);

	public abstract AImage image(File file);

	public abstract AImage image(byte[] data);

	public AParagraph(APdfElement parent, FontStyle defaultFontStyle) {
		super(parent);
		Args.assertNotNull(defaultFontStyle, "defaultFontStyle");
		this.defaultFontStyle = defaultFontStyle;
	}

	public AParagraph setSpacingTop(float spacingTop) {
		this.spacingTop = spacingTop;
		return this;
	}

	public AParagraph setSpacingBottom(float spacingBottom) {
		this.spacingBottom = spacingBottom;
		return this;
	}

	protected List<AParagraphElement> getElements() {
		return elements;
	}

	public AParagraph html(String html, FontStyle fontStyle) {
		return text(Html.convertHtmlToText(html), fontStyle);
	}

	public AParagraph html(String html) {
		return text(Html.convertHtmlToText(html));
	}

	public AParagraph text(Object text, FontStyle fontStyle) {
		Args.assertNotNull(fontStyle, "fontStyle");
		if (text == null) return this;
		elements.add(new TextChunk(this, fontStyle).text(text));
		return this;
	}

	public AParagraph text(Object text) {
		return text(text, defaultFontStyle);
	}

	public AParagraph nl() {
		text("\n");
		return this;
	}

	public AParagraph nl(FontStyle fontStyle) {
		text("\n", fontStyle);
		return this;
	}

	public AParagraph setHeight(float height) {
		this.height = height;
		return this;
	}

	public AParagraph setAlign(Align align) {
		this.align = align;
		return this;
	}

	public AParagraph setAlignRight() {
		return setAlign(Align.RIGHT);
	}

	public AParagraph setAlignCenter() {
		return setAlign(Align.CENTER);
	}

	public AParagraph setColor(Color color) {
		return setDefaultFontStyle(new FontStyle(getDefaultFontStyle()).setColor(color));
	}

	public AParagraph setDefaultFontStyle(FontStyle defaultFontStyle) {
		this.defaultFontStyle = defaultFontStyle;
		return this;
	}

	public FontStyle getDefaultFontStyle() {
		return defaultFontStyle;
	}

	// --- dependencies ---

	@Override
	public String toString() {
		return "P: " + Str.format(elements);
	}
}
