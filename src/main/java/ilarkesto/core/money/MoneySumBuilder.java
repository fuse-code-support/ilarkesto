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
package ilarkesto.core.money;

import ilarkesto.core.base.Str.Formatable;
import ilarkesto.core.base.ValueExtractor;

public class MoneySumBuilder implements Formatable {

	private Money sum;
	private Money min;
	private Money max;
	private int count;

	public MoneySumBuilder() {}

	public <T> MoneySumBuilder(Iterable<T> objects, ValueExtractor<Money, T> valueExtractor) {
		for (T o : objects) {
			add(valueExtractor.getValue(o));
		}
	}

	public MoneySumBuilder add(Money value) throws MultipleCurrenciesException {
		count++;
		if (value == null) return this;

		sum = sum == null ? value : sum.add(value);
		if (min == null || value.compareTo(min) < 0) min = value;
		if (max == null || value.compareTo(max) > 0) max = value;

		return this;
	}

	public Money getSum() {
		return sum;
	}

	public Money getAvg() {
		if (sum == null) return null;
		if (count == 0) return null;
		return sum.divideAndRound(count);
	}

	public Money getMin() {
		return min;
	}

	public Money getMax() {
		return max;
	}

	@Override
	public String format() {
		return sum == null ? null : sum.format();
	}

	public String formatMin() {
		return min == null ? null : min.format();
	}

	public String formatMax() {
		return max == null ? null : max.format();
	}

	public String formatAvg() {
		Money avg = getAvg();
		return avg == null ? null : avg.format();
	}

}
