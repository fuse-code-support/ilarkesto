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

import com.itextpdf.text.pdf.PdfPCell;

public abstract class ACell extends APdfContainerElement {

	public enum VerticalAlign {
		BASELINE, BOTTOM, MIDDLE, TOP
	}

	private int colspan = 1;

	private Color backgroundColor;

	private Color borderTopColor;
	private Color borderBottomColor;
	private Color borderLeftColor;
	private Color borderRightColor;

	private float borderTopWidth;
	private float borderBottomWidth;
	private float borderLeftWidth;
	private float borderRightWidth;

	private float paddingTop = 0;
	private float paddingBottom = 1;
	private float paddingLeft = 1;
	private float paddingRight = 1;

	private VerticalAlign verticalAlign;

	private FontStyle fontStyle;

	public ACell(APdfElement parent, FontStyle fontStyle) {
		super(parent);
		Args.assertNotNull(fontStyle, "fontStyle");
		this.fontStyle = fontStyle;
	}

	public ACell setColspan(int colspan) {
		this.colspan = colspan;
		return this;
	}

	public ACell setBorderTop(Color color, float width) {
		borderTopColor = color;
		borderTopWidth = width;
		return this;
	}

	public ACell setBorderBottom(Color color, float width) {
		borderBottomColor = color;
		borderBottomWidth = width;
		return this;
	}

	public ACell setBorderLeft(Color color, float width) {
		borderLeftColor = color;
		borderLeftWidth = width;
		return this;
	}

	public ACell setBorderRight(Color color, float width) {
		borderRightColor = color;
		borderRightWidth = width;
		return this;
	}

	public ACell setBorder(Color color, float width) {
		setBorderTop(color, width);
		setBorderBottom(color, width);
		setBorderLeft(color, width);
		setBorderRight(color, width);
		return this;
	}

	public ACell setFontStyle(FontStyle fontStyle) {
		Args.assertNotNull(fontStyle, "fontStyle");
		this.fontStyle = fontStyle;
		return this;
	}

	public ACell setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public ACell setPaddingTop(float paddingTop) {
		this.paddingTop = paddingTop;
		return this;
	}

	public ACell setPaddingRight(float paddingRight) {
		this.paddingRight = paddingRight;
		return this;
	}

	public ACell setPaddingBottom(float paddingBottom) {
		this.paddingBottom = paddingBottom;
		return this;
	}

	public ACell setPaddingLeft(float paddingLeft) {
		this.paddingLeft = paddingLeft;
		return this;
	}

	public ACell setPadding(float padding) {
		setPaddingTop(padding);
		setPaddingRight(padding);
		setPaddingBottom(padding);
		setPaddingLeft(padding);
		return this;
	}

	public ACell setPadding(float pt, float pr, float pb, float pl) {
		setPaddingTop(pt);
		setPaddingRight(pr);
		setPaddingBottom(pb);
		setPaddingLeft(pl);
		return this;
	}

	public ACell setVerticalAlign(VerticalAlign align) {
		this.verticalAlign = align;
		return this;
	}

	public VerticalAlign getVerticalAlign() {
		return this.verticalAlign;
	}

	public int getColspan() {
		return colspan;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public Color getBorderBottomColor() {
		return borderBottomColor;
	}

	public float getBorderBottomWidth() {
		return borderBottomWidth;
	}

	public Color getBorderLeftColor() {
		return borderLeftColor;
	}

	public float getBorderLeftWidth() {
		return borderLeftWidth;
	}

	public Color getBorderRightColor() {
		return borderRightColor;
	}

	public float getBorderRightWidth() {
		return borderRightWidth;
	}

	public Color getBorderTopColor() {
		return borderTopColor;
	}

	public float getBorderTopWidth() {
		return borderTopWidth;
	}

	public float getPaddingBottom() {
		return paddingBottom;
	}

	public float getPaddingLeft() {
		return paddingLeft;
	}

	public float getPaddingRight() {
		return paddingRight;
	}

	public float getPaddingTop() {
		return paddingTop;
	}

	public FontStyle getFontStyle() {
		return fontStyle;
	}

	protected int convertVerticalAlignment(VerticalAlign align) {
		switch (align) {
			case BASELINE:
				return PdfPCell.ALIGN_BASELINE;
			case BOTTOM:
				return PdfPCell.ALIGN_BOTTOM;
			case MIDDLE:
				return PdfPCell.ALIGN_MIDDLE;
			case TOP:
				return PdfPCell.ALIGN_TOP;
			default:
				throw new IllegalArgumentException("Alignment not supported");
		}
	}

	// --- dependencies ---

}
