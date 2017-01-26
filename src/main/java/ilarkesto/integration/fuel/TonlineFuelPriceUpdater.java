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
package ilarkesto.integration.fuel;

import ilarkesto.core.base.Parser;
import ilarkesto.core.base.Parser.ParseException;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.Date;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.core.time.Time;
import ilarkesto.integration.fuel.FuelStation.Price;
import ilarkesto.io.IO;
import ilarkesto.net.httpclient.HttpSession;

public class TonlineFuelPriceUpdater extends AFuelPriceUpdater {

	private static final Log log = Log.get(TonlineFuelPriceUpdater.class);

	private static final String charset = IO.ISO_LATIN_1;

	@Override
	protected void onUpdatePrices(FuelStation station) {
		String url = "http://tanken.t-online.de/tankstelle/Super/" + station.getTonlineId();

		String data;
		try {
			data = new HttpSession().setCharset(charset).downloadText(url);
		} catch (Exception ex) {
			log.error("Failed to update fuel prices:", station, ex);
			return;
		}

		updatePrice(Fuel.E5, "Super", station, data);
		updatePrice(Fuel.DIESEL, "Diesel", station, data);
		updatePrice(Fuel.E10, "Super E10", station, data);
		updatePrice(Fuel.PLUS, "SuperPlus", station, data);
		updatePrice(Fuel.AUTOGAS, "Autogas", station, data);
	}

	private Price updatePrice(String fuelId, String fuelLabel, FuelStation station, String data) {
		MyParser parser = new MyParser(data);
		try {
			Price price = parser.parsePrice(fuelLabel);
			if (price == null) return null;
			log.info("Price parsed:", station, fuelId, price, price.getPriceTicks());
			station.addPrice(fuelId, price);
			return price;
		} catch (ParseException ex) {
			log.error("Updating price failed:", fuelId, station, ex);
			return null;
		}
	}

	private class MyParser extends Parser {

		public MyParser(String data) {
			super(data);
		}

		public Price parsePrice(String typeLabel) throws ParseException {
			gotoAfter("<div class='price'>");
			gotoAfter("<div class='name'>" + typeLabel + "</div>");
			int eur = parseBigNumber();
			if (eur < 0) return null;
			int cent10 = parseBigNumber();
			int cent = parseBigNumber();
			int extra = parseSmallNumber();
			long price = (eur * 1000) + (cent10 * 100) + (cent * 10) + (extra);

			String time = parseTime();
			if (time == null) return null;
			int idx = time.indexOf(':');
			int hour = Integer.parseInt(time.substring(0, idx));
			int minute = Integer.parseInt(time.substring(idx + 1));
			DateAndTime dateAndTime = new DateAndTime(Date.today(), new Time(hour, minute));

			return new Price(price, dateAndTime.toMillis());
		}

		private String parseTime() throws ParseException {
			gotoAfter("<div class='timestamp'>");
			skipWhitespace();
			if (!isNext("Heute")) return null;
			gotoAfter(",");
			skipWhitespace();
			String time = getUntil("</div>");
			return time == null ? null : time.trim();
		}

		private int parseBigNumber() throws ParseException {
			skipWhitespace();
			gotoAfterNext("<div class='big number_");
			String s = getUntil("'");
			gotoAfter("</div>");
			if (s.equals("-")) return -1;
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException ex) {
				throw new ParseException("Number expected: " + s, pos, data);
			}
		}

		private int parseSmallNumber() throws ParseException {
			skipWhitespace();
			gotoAfterNext("<div class='");
			String s;
			if (isNext("small ")) {
				gotoAfterNext("small ");
				s = getUntil("'");
			} else {
				s = getUntil(" ");
			}

			if (s.startsWith("number_")) {
				String num = s.substring(s.indexOf('_') + 1);
				return Integer.parseInt(num);
			}

			if (s.equals("nine")) return 9;
			if (s.equals("eight")) return 8;
			if (s.equals("seven")) return 7;
			if (s.equals("six")) return 6;
			if (s.equals("five")) return 5;
			if (s.equals("four")) return 4;
			if (s.equals("three")) return 3;
			if (s.equals("two")) return 2;
			if (s.equals("one")) return 1;
			if (s.equals("zero")) return 0;
			throw new ParseException("Unsuported number: " + s, pos, s);
		}
	}

}
