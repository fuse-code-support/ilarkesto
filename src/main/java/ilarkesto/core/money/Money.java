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
package ilarkesto.core.money;

import ilarkesto.core.base.Args;
import ilarkesto.core.base.Str;
import ilarkesto.core.base.Str.Formatable;
import ilarkesto.core.base.Utl;
import ilarkesto.core.localization.Localizer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Working with money (amount + currency).
 */
public final class Money implements Comparable<Money>, Serializable, Formatable {

	public static final transient String EUR = "EUR";
	public static final transient String USD = "USD";

	private long cent;
	private String currency;

	public Money(long value, long cent, String currency) {
		Args.assertNotNull(currency, "currency");
		this.cent = (value * 100) + cent;
		this.currency = parseCurrency(currency);
	}

	public Money(String s) {
		this(s, false);
	}

	public Money(String s, boolean roundHalfUp) {
		int amountCurrencySeparatorIdx = s.indexOf(' ');
		if (amountCurrencySeparatorIdx < 1) throw new IllegalArgumentException("Unsupported money format: " + s);

		this.currency = parseCurrency(s.substring(amountCurrencySeparatorIdx));

		String amount = s.substring(0, amountCurrencySeparatorIdx).trim().replace(',', '.');
		BigDecimal bd = new BigDecimal(amount).movePointRight(2);
		if (roundHalfUp) bd = bd.setScale(0, RoundingMode.HALF_UP);
		this.cent = bd.longValueExact();
	}

	public Money(String amount, String currency) {
		this(amount, currency, false);
	}

	public Money(String amount, String currency, boolean roundHalfUp) {
		this(amount + " " + currency, roundHalfUp);
	}

	@Deprecated
	public Money(double value, String currency) {
		Args.assertNotNull(currency, "currency");
		this.currency = parseCurrency(currency);
		this.cent = Math.round(value * 100);
	}

	private static String parseCurrency(String currency) {
		Args.assertNotNull(currency, "currency");
		currency = currency.trim();
		if (currency.equals("€")) currency = "EUR";
		if (currency.equals("$")) currency = "USD";
		if (currency.length() != 3)
			throw new IllegalArgumentException("Unsupported currency. Only USD, EUR, PLN,... allowed. -> " + currency);
		currency = currency.toUpperCase();
		return currency;
	}

	public Money(BigDecimal value, String currency) {
		this(value.toPlainString() + " " + currency, true);
	}

	public Money(long value, String currency) {
		this(value, 0, currency);
	}

	public Money() {
		this(0, Money.EUR);
	}

	public String getCurrency() {
		return currency;
	}

	public boolean isCurrency(String currency) {
		return this.currency.equals(currency);
	}

	@Deprecated
	public float getAmountAsFloat() {
		return cent / 100f;
	}

	@Deprecated
	public double getAmountAsDouble() {
		return cent / 100d;
	}

	public long getAmountAsCent() {
		return cent;
	}

	public BigDecimal getAmount() {
		return new BigDecimal(cent).setScale(2).divide(Utl.BD_HUNDRED, BigDecimal.ROUND_HALF_UP);
	}

	public boolean isPositive() {
		return cent >= 0;
	}

	public boolean isNegative() {
		return cent < 0;
	}

	public Money negate() {
		return new Money(0, cent * -1, currency);
	}

	public Money substract(Money subtrahend) throws MultipleCurrenciesException {
		return computeDifference(this, subtrahend);
	}

	public Money subtract(Money... subtrahends) throws MultipleCurrenciesException {
		return computeDifference(this, subtrahends);
	}

	public Money subtractPercent(BigDecimal percent) {
		if (percent == null) return this;
		try {
			return subtract(percent(percent));
		} catch (MultipleCurrenciesException ex) {
			throw new RuntimeException(ex);
		}
	}

	public Money add(Money summand) throws MultipleCurrenciesException {
		if (summand == null) return this;
		return computeSum(this, summand);
	}

	public Money invert() {
		return new Money(0, cent * -1, currency);
	}

	public Money multiply(BigDecimal multiplicand) {
		if (multiplicand == null) return null;
		BigDecimal value = getAmount().multiply(multiplicand);
		return new Money(value.setScale(2, RoundingMode.HALF_UP), currency);
	}

	public Money multiplyAndRound(float factor) {
		return new Money(0, Math.round((cent * factor)), currency);
	}

	public Money multiplyAndRound(double factor) {
		return new Money(0, Math.round((cent * factor)), currency);
	}

	@Deprecated
	public Money divideAndRound(float divisor) {
		return new Money(0, Math.round((cent / divisor)), currency);
	}

	public Money divideAndRound(BigDecimal divisor) {
		if (Utl.isZeroOrNull(divisor)) return null;
		return new Money(getAmount().divide(divisor, BigDecimal.ROUND_HALF_UP), currency);
	}

	public String getAmountAsString(char decimalSeparator) {
		return getAmountAsString(decimalSeparator, null);
	}

	public String getAmountAsString(char decimalSeparator, String thousandsSeparator) {
		boolean neg = false;
		long c = cent;
		if (c < 0) {
			neg = true;
			c = Math.abs(c);
		}
		long value = c / 100;
		long rest = c - (value * 100);
		StringBuilder sb = new StringBuilder();
		if (neg) sb.append("-");
		sb.append(Str.formatWithThousandsSeparator(value, thousandsSeparator));
		sb.append(decimalSeparator);
		if (rest < 10) sb.append('0');
		sb.append(rest);
		return sb.toString();
	}

	@Override
	public String format() {
		return format(true);
	}

	public String format(boolean showCurrency) {
		return Localizer.get().format(this, showCurrency);
	}

	public String toString(char decimalSeparator) {
		return getAmountAsString(decimalSeparator) + ' ' + currency;
	}

	public boolean isLessThen(Money m) {
		return cent < m.cent;
	}

	public boolean isLessThenOrEqualTo(Money m) {
		return cent <= m.cent;
	}

	public boolean isGreaterThen(Money m) {
		return cent > m.cent;
	}

	public boolean isGreaterThenOrEqualTo(Money m) {
		return cent >= m.cent;
	}

	@Override
	public String toString() {
		return toString('.');
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		Money other = (Money) obj;
		return cent == other.cent && currency.equals(other.currency);
	}

	private transient int hashCode;

	@Override
	public int hashCode() {
		if (hashCode == 0) {
			hashCode = 23;
			hashCode = hashCode * 37 + (int) cent;
			hashCode = hashCode * 37 + currency.hashCode();
		}
		return hashCode;
	}

	@Override
	public int compareTo(Money o) {
		if (cent > o.cent) return 1;
		if (cent < o.cent) return -1;
		return 0;
	}

	public BigDecimal getPercentageOf(Money total) {
		if (total == null) return null;
		if (total.isZero()) return null;
		return new BigDecimal(cent).divide(total.getAmount(), BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getPercentage(Money percentage) {
		if (percentage == null) return null;
		return percentage.getPercentageOf(this);
	}

	public Money getPercentage(BigDecimal percentage) {
		if (percentage == null) return null;
		if (percentage.compareTo(BigDecimal.ZERO) == 0) return new Money(0, currency);
		return new Money(percentage.multiply(getAmount()).divide(Utl.BD_HUNDRED, BigDecimal.ROUND_HALF_UP), currency);
	}

	// --- ---

	public static Money computeAvg(String currency, Money... moneys) throws MultipleCurrenciesException {
		return computeSum(currency, moneys).divideAndRound(moneys.length);
	}

	public static Money computeSum(Money... moneys) throws MultipleCurrenciesException {
		if (moneys.length == 0) throw new IllegalArgumentException("At least one money required for computing a sum.");
		if (moneys.length == 1) return moneys[0];

		String currency = null;
		for (int i = 0; currency == null && i < moneys.length; i++) {
			if (moneys[i] == null) continue;
			currency = moneys[i].currency;
		}
		if (currency == null) return null;
		return computeSum(currency, moneys);
	}

	public static Money computeSum(String currency, Money... summands) throws MultipleCurrenciesException {
		if (summands.length == 1) return summands[0];
		long sum = 0;
		for (Money summand : summands) {
			if (summand == null) continue;
			if (!summand.isCurrency(currency)) throw new MultipleCurrenciesException(currency, summand.getCurrency());
			sum += summand.cent;
		}
		return new Money(0, sum, currency);
	}

	public static Money computeDifference(Money minuend, Money... subtrahends) throws MultipleCurrenciesException {
		if (minuend == null) return null;
		if (subtrahends == null || subtrahends.length == 0) return minuend;

		long difference = minuend.cent;
		for (Money subtrahend : subtrahends) {
			if (subtrahend == null) continue;
			if (!subtrahend.isCurrency(minuend.currency))
				throw new MultipleCurrenciesException(minuend.currency, subtrahend.currency);
			difference -= subtrahend.cent;
		}
		return new Money(0, difference, minuend.currency);
	}

	public static Money[] createArray(int size, Money initialValue) {
		Money[] moneys = new Money[size];
		for (int i = 0; i < size; i++)
			moneys[i] = initialValue;
		return moneys;
	}

	public Money percent(BigDecimal percent) {
		if (percent == null) return null;
		BigDecimal multiplicant = percent.divide(Utl.BD_HUNDRED);
		return multiply(multiplicant);
	}

	public boolean isZero() {
		return cent == 0;
	}

}
