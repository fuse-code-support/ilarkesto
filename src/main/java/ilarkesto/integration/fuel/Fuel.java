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

import ilarkesto.core.time.Time;

import java.util.ArrayList;
import java.util.List;

public class Fuel {

	public static final String DIESEL = "DIESEL";
	public static final String E5 = "E5";
	public static final String E10 = "E10";
	public static final String PLUS = "PLUS";
	public static final String AUTOGAS = "AUTOGAS";

	public static String getFuelLabel(String fuelType) {
		if (DIESEL.equals(fuelType)) return "Diesel";
		if (E5.equals(fuelType)) return "Super 95";
		if (E10.equals(fuelType)) return "Super E10";
		if (PLUS.equals(fuelType)) return "Super Plus";
		if (AUTOGAS.equals(fuelType)) return "Autogas";
		return fuelType;
	}

	public static List<FuelStation> createRintelnStations() {
		List<FuelStation> ret = new ArrayList<FuelStation>();

		ret.add(new FuelStation("Jet (Mc Donalds)", "Konrad-Adenauer-Straße 46, Rinteln", "5852963600277ea193369b18")
				.addServiceTime(5, 24));

		ret.add(new FuelStation("Aral (Kino)", "Auf Der Bünte 1, Rinteln", "5852963800277ea193369b68").addServiceTime(6,
			22));

		ret.add(new FuelStation("Esso (Marktkauf)", "Konrad-Adenauer-Straße 24, Rinteln", "5852968f00277ea19336aff1"));

		ret.add(new FuelStation("Esso (Friedhof)", "Seetorstraße 16, Rinteln", "5852968f00277ea19336b001")
				.addServiceTime(6, 22));

		ret.add(new FuelStation("Jantzon (Eisbergen)", "Weserstraße 83, Porta-Westfalica", "585296cf00277ea19336c085"));

		ret.add(new FuelStation("Shell (Luhden, A2)", "An der B 83, Luhden", "5852963800277ea193369b7f"));

		ret.add(
			new FuelStation("Harting (Kleinbremen)", "Bremer Straße 4, Porta Westfalica", "585296ae00277ea19336b829")
					.addServiceTime(new Time(7, 30), new Time(18, 30)));

		ret.add(new FuelStation("Shell (Möllenbeck)", "Lemgoer Straße 55, Rinteln", "5852963800277ea193369b80")
				.addServiceTime(6, 22));

		ret.add(new FuelStation("Reiffeisen (Krankenhagen)", "Extertalstraße 10c, Rinteln", "585296e500277ea19336c648")
				.addServiceTime(6, 22));

		return ret;
	}

	public static List<FuelStation> createBueckeburgStations() {
		List<FuelStation> ret = new ArrayList<FuelStation>();

		ret.add(
			new FuelStation("Jantzon & Hocke (Bückeburg)", "Röcker Straße 24, Bückeburg", "585296cf00277ea19336c082")
					.addServiceTime(6, 22));

		ret.add(new FuelStation("HEM (Bückeburg)", "Hannoversche Straße 16, Bückeburg", "5852968100277ea19336ac24"));

		ret.add(new FuelStation("Alte Molkerei (Bückeburg)", "Hannoversche Straße 15, Bückeburg",
				"585296a200277ea19336b534").addServiceTime(new Time(6, 0), new Time(22, 30)));

		ret.add(new FuelStation("Westfalen (Bückeburg)", "Petzer Straße 6c, Bückeburg", "5852963800277ea193369b91")
				.addServiceTime(6, 22));

		ret.add(new FuelStation("Q1 (Bückeburg)", "Steinberger Straße 36, Bückeburg", "5852968000277ea19336abf0")
				.addServiceTime(6, 22));

		return ret;
	}

	public static List<FuelStation> createAuetalStations() {
		List<FuelStation> ret = new ArrayList<FuelStation>();

		ret.add(new FuelStation("Classic (Auetal)", "Kathrinhagener Straße 19, Auetal", "5852968400277ea19336ace2")
				.addServiceTime(new Time(7, 30), new Time(18, 30)));

		return ret;
	}

	public static List<FuelStation> createExtertalStations() {
		List<FuelStation> ret = new ArrayList<FuelStation>();

		ret.add(new FuelStation("Shell (Extertal)", "Zum Goldenen Winkel 1, Extertal", "5852964400277ea193369e3d")
				.addServiceTime(7, 21));

		return ret;
	}
}
