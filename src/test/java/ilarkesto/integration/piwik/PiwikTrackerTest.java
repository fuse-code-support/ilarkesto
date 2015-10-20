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
package ilarkesto.integration.piwik;

import ilarkesto.base.Utl;
import ilarkesto.testng.ATest;

import org.testng.annotations.Test;

public class PiwikTrackerTest extends ATest {

	private PiwikTracker tracker = new PiwikTracker("http://piwik.servisto.de", 3,
			"http://ilarkesto-test.piwik.servisto.de");

	@Test
	public void action() {
		tracker.request("test-action").actionName("action1").post();
	}

	@Test
	public void event() {
		for (int i = 0; i < 10; i++) {
			tracker.request("test-event")
					.event("category" + +Utl.randomInt(1, 3), "action-" + Utl.randomInt(1, 3),
						"name-" + +Utl.randomInt(1, 3), Utl.randomInt(1, 3)).queue();
		}
		tracker.flush();
	}

}
