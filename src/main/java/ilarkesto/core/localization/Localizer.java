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
package ilarkesto.core.localization;

import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.core.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Localizer {

	public static final Localizer EN = new Localizer();
	public static final Localizer DE = new DeLocalizer();
	public static final Localizer PL = new PlLocalizer();

	private static Log log = Log.get(Localizer.class);

	private static Localizer current = EN;

	protected Localizer() {}

	public String getLanguage() {
		return "en";
	}

	public char getDecimalSeparator() {
		return '.';
	}

	public char getThousandsSeparator() {
		return ',';
	}

	public String formatPercent(BigDecimal value, int scale) {
		if (value == null) return null;
		BigDecimal bd = value.setScale(scale, RoundingMode.HALF_UP);
		return format(bd, false) + " %";
	}

	public String format(Money money) {
		return format(money, true);
	}

	public String format(Money money, boolean showCurrency) {
		if (money == null) return null;

		if (showCurrency)
			return format(money.getAmount(), true, 2, false) + " " + formatCurrency(money.getCurrency());
		else return format(money.getAmount(), true, 2, false);
	}

	public String formatCurrency(String currencyCode) {
		if (currencyCode == null) return null;
		currencyCode = currencyCode.toUpperCase();
		if (currencyCode.equals("EUR")) return "€";
		if (currencyCode.equals("USD")) return "$";
		return currencyCode;
	}

	public String format(Number value, boolean thousandsSeparator) {
		return format(value, thousandsSeparator, null, true);
	}

	public String format(Number value, boolean thousandsSeparator, Integer scale, boolean stripTrailingZeros) {
		if (value == null) return null;
		String ret;
		if (value instanceof BigDecimal) {
			BigDecimal bd = (BigDecimal) value;
			if (scale != null && bd.scale() != scale) {
				bd = bd.setScale(scale, BigDecimal.ROUND_HALF_UP);
			}
			if (stripTrailingZeros) bd = bd.stripTrailingZeros();
			ret = bd.toPlainString();
		} else {
			ret = value.toString();
		}

		char decimalSeparator = getDecimalSeparator();
		if (decimalSeparator != '.') ret = ret.replace('.', decimalSeparator);

		if (!thousandsSeparator) return ret;

		int decimalSeparatorIdx = ret.indexOf(decimalSeparator);
		if (decimalSeparatorIdx < 0) decimalSeparatorIdx = ret.length();

		boolean negative = ret.startsWith("-");
		if (negative) {
			ret = ret.substring(1);
			decimalSeparatorIdx--;
		}

		if (decimalSeparatorIdx >= 4) {
			ret = Str.insert(ret, decimalSeparatorIdx - 3, getThousandsSeparator());
			if (decimalSeparatorIdx >= 7) {
				ret = Str.insert(ret, decimalSeparatorIdx - 6, getThousandsSeparator());
				if (decimalSeparatorIdx >= 10) {
					ret = Str.insert(ret, decimalSeparatorIdx - 9, getThousandsSeparator());
					if (decimalSeparatorIdx >= 13) {
						ret = Str.insert(ret, decimalSeparatorIdx - 12, getThousandsSeparator());
					}
				}
			}
		}

		if (negative) return "-" + ret;
		return ret;
	}

	@Override
	public String toString() {
		return getLanguage();
	}

	public static Localizer get() {
		return current;
	}

	public static void setCurrent(Localizer localizer) {
		Localizer.current = localizer;
		log.info("Localizer switched to:", localizer);
	}

	public static void setCurrent(String language, Localizer fallback) {
		setCurrent(forLanguage(language, fallback));
	}

	public static Localizer forLanguage(String language, Localizer fallback) {
		language = language.toLowerCase();
		if ("en".equals(language)) return EN;
		if ("de".equals(language)) return DE;
		if ("pl".equals(language)) return PL;
		return fallback;
	}

}
