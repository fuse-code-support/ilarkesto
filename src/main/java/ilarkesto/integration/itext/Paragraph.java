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
package ilarkesto.integration.itext;

import java.io.File;
import java.util.Set;

import com.google.gwt.dev.util.collect.HashSet;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import ilarkesto.core.logging.Log;
import ilarkesto.pdf.AImage;
import ilarkesto.pdf.AParagraph;
import ilarkesto.pdf.AParagraphElement;
import ilarkesto.pdf.APdfElement;
import ilarkesto.pdf.FontStyle;
import ilarkesto.pdf.TextChunk;

public class Paragraph extends AParagraph implements ItextElement {

	public static boolean embedInternationalFonts = false;

	private static Log log = Log.get(Paragraph.class);

	Paragraph(APdfElement parent, FontStyle defaultFontStyle) {
		super(parent, defaultFontStyle);
	}

	@Override
	public Element[] createITextElements(Document document) {
		com.itextpdf.text.Paragraph p = new com.itextpdf.text.Paragraph();
		float maxSize = 0;
		FontSelector fontSelector = new FontSelector();
		Set<String> fontsAddedToSelector = new HashSet<String>();
		for (AParagraphElement element : getElements()) {
			if (element instanceof TextChunk) {
				TextChunk textChunk = (TextChunk) element;
				FontStyle fontStyle = textChunk.getFontStyle();

				String fontIdentifier = fontStyle.getFont() + fontStyle.getSize() + fontStyle.getColor()
						+ fontStyle.isItalic() + fontStyle.isBold();
				if (!fontsAddedToSelector.contains(fontIdentifier)) {
					addFont(fontSelector, fontStyle);
					fontsAddedToSelector.add(fontIdentifier);
				}

				String text = textChunk.getText();
				Phrase phrase = fontSelector.process(text);
				p.add(phrase);

				float size = (fontStyle.getSize() * 1.1f) + 1f;
				if (size > maxSize) maxSize = PdfBuilder.mmToPoints(size);
			} else if (element instanceof Image) {
				Image image = (Image) element;
				com.itextpdf.text.Image itextImage;
				try {
					itextImage = (com.itextpdf.text.Image) image.createITextElements(document)[0];
				} catch (Exception ex) {
					log.warn("Including image failed:", image, ex);
					continue;
				}

				if (image.getAlign() != null) {
					itextImage.setAlignment(Image.convertAlign(image.getAlign()) | com.itextpdf.text.Image.TEXTWRAP);
					p.add(itextImage);
				} else {
					Chunk chunk = new Chunk(itextImage, 0, 0);
					p.add(chunk);
					float size = image.getHeight(document) + 3;
					if (size > maxSize) maxSize = size;
				}

			} else {
				throw new RuntimeException("Unsupported paragraph element: " + element.getClass().getName());
			}
		}
		p.setLeading(maxSize);
		p.setSpacingBefore(PdfBuilder.mmToPoints(spacingTop));
		p.setSpacingAfter(PdfBuilder.mmToPoints(spacingBottom));
		if (align != null) p.setAlignment(convertAlign(align));
		if (height <= 0) return new Element[] { p };

		// wrap in table
		PdfPCell cell = new PdfPCell();
		cell.setBorder(0);
		cell.setFixedHeight(PdfBuilder.mmToPoints(height));
		cell.addElement(p);
		PdfPTable table = new PdfPTable(1);
		table.setWidthPercentage(100);
		table.addCell(cell);
		return new Element[] { table };
	}

	private int createStyle(FontStyle fontStyle) {
		int style = Font.NORMAL;
		if (fontStyle.isItalic() && fontStyle.isBold()) {
			style = Font.BOLDITALIC;
		} else if (fontStyle.isItalic()) {
			style = Font.ITALIC;
		} else if (fontStyle.isBold()) {
			style = Font.BOLD;
		}
		return style;
	}

	public void addFont(FontSelector selector, FontStyle fontStyle) {
		log.info("creating new font:", fontStyle);

		selector.addFont(createFont(fontStyle.getFont(), BaseFont.IDENTITY_H, fontStyle));

		if (embedInternationalFonts) {
			// embeddable chinese
			selector.addFont(createFont("fonts/HDZB_36.ttf", BaseFont.IDENTITY_H, fontStyle));

			// fallback from iTextAsian.jar
			selector.addFont(createFont("STSong-Light", "UniGB-UCS2-H", fontStyle)); // simplified chinese

			// traditional chinese
			selector.addFont(createFont("MHei-Medium", BaseFont.IDENTITY_H, fontStyle));

			// japanese
			selector.addFont(createFont("HeiseiMin-W3", BaseFont.IDENTITY_H, fontStyle));

			// japanese
			selector.addFont(createFont("KozMinPro-Regular", BaseFont.IDENTITY_H, fontStyle));

			// korean
			selector.addFont(createFont("HYGoThic-Medium", BaseFont.IDENTITY_H, fontStyle));
		}
	}

	private Font createFont(String name, String encoding, FontStyle fontStyle) {
		Font font;
		try {
			font = new Font(BaseFont.createFont(name, encoding, BaseFont.EMBEDDED));
		} catch (Exception ex) {
			throw new RuntimeException("Loading font failed: " + name, ex);
		}

		if (fontStyle != null) {
			font.setStyle(createStyle(fontStyle));
			font.setSize(PdfBuilder.mmToPoints(fontStyle.getSize()));
			font.setColor(PdfBuilder.color(fontStyle.getColor()));
		}

		return font;
	}

	@Override
	public AImage image(byte[] data) {
		Image i = new Image(this, data);
		elements.add(i);
		return i;
	}

	@Override
	public AImage image(File file) {
		Image i = new Image(this, file);
		elements.add(i);
		return i;
	}

	private static int convertAlign(Align align) {
		switch (align) {
			case LEFT:
				return com.itextpdf.text.Paragraph.ALIGN_LEFT;
			case CENTER:
				return com.itextpdf.text.Paragraph.ALIGN_CENTER;
			case RIGHT:
				return com.itextpdf.text.Paragraph.ALIGN_RIGHT;
		}
		throw new RuntimeException("Unsupported align: " + align);
	}

	// --- dependencies ---

}
