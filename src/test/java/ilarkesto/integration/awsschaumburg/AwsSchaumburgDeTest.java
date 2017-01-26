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
package ilarkesto.integration.awsschaumburg;

import ilarkesto.core.base.OperationObserver;
import ilarkesto.core.base.Parser.ParseException;
import ilarkesto.core.logging.Log;
import ilarkesto.integration.awsschaumburg.WastePickupSchedule.Pickup;
import ilarkesto.net.httpclient.HttpSession;
import ilarkesto.testng.ATest;

import java.util.List;

import org.testng.annotations.Test;

public class AwsSchaumburgDeTest extends ATest {

	private Log log = Log.get(AwsSchaumburgDe.class);

	@Test
	public void update() throws ParseException {
		WastePickupArea area = new WastePickupArea("Unter der Frankenburg", 2083);
		WastePickupSchedule schedule = new WastePickupSchedule(area);
		AwsSchaumburgDe.update(OperationObserver.DUMMY, schedule);
		List<Pickup> pickups = schedule.getPickups();
		log.info(pickups);
		assertNotEmpty(pickups);
	}

	@Test
	public void loadPickupAreas() throws ParseException {
		HttpSession.debug = true;
		List<WastePickupArea> areas = AwsSchaumburgDe.loadPickupAreas();
		log.info(areas);
		assertSize(areas, 231);
	}

}
