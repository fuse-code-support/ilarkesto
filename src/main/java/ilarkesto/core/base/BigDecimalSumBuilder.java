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
package ilarkesto.core.base;

import ilarkesto.core.base.Str.Formatable;
import ilarkesto.core.localization.Localizer;

import java.math.BigDecimal;

public class BigDecimalSumBuilder implements Formatable {

	private BigDecimal sum;
	private BigDecimal min;
	private BigDecimal max;
	private int count;

	public BigDecimalSumBuilder add(Number value) {
		if (value == null) return this;
		count++;
		if (value == null) return this;
		BigDecimal bdValue = ((value instanceof BigDecimal) ? (BigDecimal) value : new BigDecimal(value.toString()));
		sum = sum == null ? bdValue : sum.add(bdValue);
		if (min == null || bdValue.compareTo(min) < 0) min = bdValue;
		if (max == null || bdValue.compareTo(max) > 0) max = bdValue;
		return this;
	}

	public BigDecimal getSum() {
		return sum;
	}

	public BigDecimal getAvg(int scale) {
		if (sum == null) return null;
		if (count == 0) return null;
		return sum.divide(new BigDecimal(count), scale, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getMin() {
		return min;
	}

	public BigDecimal getMax() {
		return max;
	}

	@Override
	public String format() {
		return sum == null ? null : Localizer.get().format(sum, true, 2, true);
	}

}
