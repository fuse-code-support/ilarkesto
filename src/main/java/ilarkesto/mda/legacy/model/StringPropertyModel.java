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
package ilarkesto.mda.legacy.model;

public class StringPropertyModel extends SimplePropertyModel {

	private boolean richtext;
	private boolean multiline;
	private String codemirrorMode;
	private Integer maxLenght;
	private boolean templateAvailable;
	private boolean masked;
	private boolean encrypted;
	private boolean outsourced;

	public StringPropertyModel(BeanModel entityModel, String name) {
		super(entityModel, name, false, false, String.class.getName());
	}

	public StringPropertyModel setMasked(boolean masked) {
		this.masked = masked;
		return this;
	}

	public StringPropertyModel setOutsourced(boolean outsourced) {
		this.outsourced = outsourced;
		return this;
	}

	public boolean isOutsourced() {
		return outsourced;
	}

	public boolean isMasked() {
		return masked;
	}

	public StringPropertyModel setRichtext(boolean richtext) {
		this.richtext = richtext;
		if (richtext) setMultiline(true);
		return this;
	}

	public StringPropertyModel setMultiline(boolean multiline) {
		this.multiline = multiline;
		return this;
	}

	public StringPropertyModel setCodemirrorMode(String mode) {
		this.codemirrorMode = mode;
		return this;
	}

	public boolean isRichtext() {
		return richtext;
	}

	public boolean isMultiline() {
		return multiline;
	}

	public String getCodemirrorMode() {
		return codemirrorMode;
	}

	public StringPropertyModel setMaxLenght(Integer maxLenght) {
		this.maxLenght = maxLenght;
		return this;
	}

	public Integer getMaxLenght() {
		return maxLenght;
	}

	public boolean isMaxLengthSet() {
		return maxLenght != null;
	}

	public StringPropertyModel setTemplateAvailable(boolean template) {
		this.templateAvailable = template;
		return this;
	}

	public boolean isTemplateAvailable() {
		return templateAvailable;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public StringPropertyModel setEncrypted(boolean b) {
		this.encrypted = b;
		return this;
	}

}
