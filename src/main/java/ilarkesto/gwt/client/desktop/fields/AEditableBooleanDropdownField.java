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
package ilarkesto.gwt.client.desktop.fields;

import ilarkesto.core.base.EnumMapper;
import ilarkesto.core.base.Str;

import java.util.Arrays;
import java.util.List;

public abstract class AEditableBooleanDropdownField extends AEditableSelectOneField {

	private String trueLabel = "Ja";
	private String falseLabel = "Nein";

	protected abstract void applyValue(boolean value);

	protected abstract boolean getValue();

	@Override
	public final void applyValue(String value) {
		applyValue(Str.isTrue(value));
	}

	@Override
	public EnumMapper<String, String> getOptions() {
		return mapper;
	}

	@Override
	public String getSelectedOptionKey() {
		return String.valueOf(getValue());
	}

	protected String getFalseLabel() {
		return falseLabel;
	}

	protected String getTrueLabel() {
		return trueLabel;
	}

	public AEditableBooleanDropdownField setLabels(String trueLabel, String falseLabel) {
		this.trueLabel = trueLabel;
		this.falseLabel = falseLabel;
		return this;
	}

	@Override
	public boolean isMandatory() {
		return true;
	}

	private EnumMapper<String, String> mapper = new EnumMapper<String, String>() {

		@Override
		public List<String> getKeys() {
			return Arrays.asList(String.valueOf(true), String.valueOf(false));
		}

		@Override
		public String getValueForKey(String key) {
			return Str.isTrue(key) ? trueLabel : falseLabel;
		}
	};

}
