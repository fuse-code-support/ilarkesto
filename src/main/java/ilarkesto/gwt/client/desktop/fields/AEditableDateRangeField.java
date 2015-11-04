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

import ilarkesto.core.base.UserInputException;
import ilarkesto.core.time.Date;
import ilarkesto.core.time.DateRange;
import ilarkesto.gwt.client.desktop.Widgets;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.datepicker.client.DateBox;

public abstract class AEditableDateRangeField extends AEditableField {

	public static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy");

	private GoonDateBox startBox;
	private GoonDateBox endBox;
	private Label daysLabel;

	private DateRange previousValue;

	protected abstract DateRange getValue();

	public abstract void applyValue(DateRange value);

	@Override
	public boolean isValueSet() {
		return getValue() != null;
	}

	protected void validateValue(DateRange value) {
		if (value == null && isMandatory()) throw new RuntimeException("Eingabe erforderlich.");
	}

	@Override
	public void trySubmit() throws UserInputException {
		Date start = prepareValue(startBox.getText());
		Date end = prepareValue(endBox.getText());
		if (end == null) end = start;
		DateRange value = start == null ? null : new DateRange(start, end);
		validateValue(value);
		applyValue(value);
	}

	@Override
	protected IsWidget createEditorWidget() {
		startBox = new GoonDateBox(new DateBox.DefaultFormat(format));
		startBox.getElement().setId(getId() + "_start_textBox");
		startBox.getElement().getStyle().setWidth(100, Unit.PX);
		startBox.setMaxLength(10);
		startBox.addKeyUpHandler(new EnterKeyUpHandler());

		endBox = new GoonDateBox(new DateBox.DefaultFormat(format));
		endBox.getElement().setId(getId() + "_end_textBox");
		endBox.getElement().getStyle().setWidth(100, Unit.PX);
		endBox.setMaxLength(10);
		endBox.addKeyUpHandler(new EnterKeyUpHandler());

		DateRange value = getValue();
		previousValue = value;

		if (value != null) {
			startBox.setValue(value.getStart().formatDayMonthYear());
			endBox.setValue(value.getEnd().formatDayMonthYear());
		}

		startBox.addValueChangeHandler(new ValueChangeHandler<java.util.Date>() {

			@Override
			public void onValueChange(ValueChangeEvent<java.util.Date> event) {
				java.util.Date date = event.getValue();
				onStartInputChanged(date == null ? null : new Date(date));
				updateDaysLabel();
				updatePreviousValue();
			}

		});

		endBox.addValueChangeHandler(new ValueChangeHandler<java.util.Date>() {

			@Override
			public void onValueChange(ValueChangeEvent<java.util.Date> event) {
				java.util.Date date = event.getValue();
				onEndInputChanged(date == null ? null : new Date(date));
				updateDaysLabel();
				updatePreviousValue();
			}

		});

		daysLabel = new Label();
		daysLabel.getElement().getStyle().setLineHeight(35, Unit.PX);
		updateDaysLabel();

		Label bis = Widgets.text("bis");
		bis.getElement().getStyle().setLineHeight(35, Unit.PX);
		return Widgets.horizontalFlowPanel(Widgets.defaultSpacing, startBox, bis, endBox, daysLabel);
	}

	private void updatePreviousValue() {
		Date start;
		try {
			start = prepareValue(startBox.getText());
		} catch (UserInputException ex) {
			return;
		}
		if (start == null) return;
		Date end;
		try {
			end = prepareValue(endBox.getText());
		} catch (UserInputException ex) {
			return;
		}
		if (end == null) end = start;
		try {
			previousValue = new DateRange(start, end);
		} catch (Exception ex) {}
	}

	private void updateDaysLabel() {
		String days;
		try {
			days = computeDays();
		} catch (UserInputException ex) {
			days = ex.getMessage();
		}
		daysLabel.setText(days);
	}

	private String computeDays() throws UserInputException {
		Date start = prepareValue(startBox.getText());
		if (start == null) return null;
		Date end = prepareValue(endBox.getText());
		if (end == null) end = start;
		try {
			return new DateRange(start, end).getDayCount() + " Tage";
		} catch (Exception ex) {
			return null;
		}
	}

	private void onStartInputChanged(Date start) {
		if (start == null) return;
		if (previousValue == null) return;
		int daysDiff = previousValue.getDayCount() - 1;
		endBox.setValue(start.addDays(daysDiff).formatDayMonthYear());
	}

	private void onEndInputChanged(Date end) {
		if (end == null) return;
	}

	@Override
	public IsWidget createDisplayWidget() {
		DateRange value = getValue();
		return value == null ? null : Widgets.text(value.formatShortest());
	}

	private Date prepareValue(String value) throws UserInputException {
		if (value == null) return null;
		value = value.trim();
		if (value.isEmpty()) return null;

		java.util.Date javaDate;
		try {
			javaDate = format.parse(value);
		} catch (Exception ex) {
			throw new UserInputException("Eingabe muß ein Datum sein. TT.MM.JJJJ, z.B. 01.01.2001");
		}
		Date date = new Date(javaDate);
		if (date.getYear() > 9999)
			throw new UserInputException("Eingabe muß ein Datum sein. TT.MM.JJJJ, z.B. 01.01.2001");
		if (date.getYear() < 100) date = date.addYears(2000);
		if (date.getYear() < 1000)
			throw new RuntimeException("Eingabe muß ein Datum sein. TT.MM.JJJJ, z.B. 01.01.2001");
		return date;
	}

	private class EnterKeyUpHandler implements KeyUpHandler {

		@Override
		public void onKeyUp(KeyUpEvent event) {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				submitOrParentSubmit();
			}
		}

	}

}
