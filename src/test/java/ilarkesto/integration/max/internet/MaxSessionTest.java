package ilarkesto.integration.max.internet;

import ilarkesto.core.time.Date;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.core.time.Time;
import ilarkesto.core.time.Weekday;
import ilarkesto.integration.max.state.MaxCubeState;
import ilarkesto.integration.max.state.MaxDevice;
import ilarkesto.integration.max.state.MaxHouse;
import ilarkesto.integration.max.state.MaxRoom;
import ilarkesto.integration.max.state.MaxWeekTemperatureProfile;
import ilarkesto.testng.ATest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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

public class MaxSessionTest extends ATest {

	private String username;
	private String password;

	@BeforeClass
	public void loadCredentials() throws FileNotFoundException, IOException {
		File file = new File("runtimedata/max.properties");
		if (!file.exists()) return;
		Properties properties = new Properties();
		properties.load(new BufferedReader(new FileReader(file)));
		username = properties.getProperty("username");
		password = properties.getProperty("password");
	}

	@Test
	public void changeTemp() {
		MaxSession session = MaxSession.createElvInstance();
		session.login(username, password);
		MaxCubeState state = session.getMaxCubeState();
		System.out.println(state.toString());

		MaxRoom room = state.getRoomById(3);
		session.executeSetRoomTemporaryMode(room, 16f, new DateAndTime(Date.today(), new Time(17, 00)).toJavaDate());
	}

	// @Test
	public void initialize() {
		MaxSession pc = MaxSession.createElvInstance();
		pc.initialize();
		String scriptSessionId = pc.getScriptSessionId();
		System.out.println(scriptSessionId);
		assertNotNull(scriptSessionId);
	}

	// @Test
	public void login() {
		MaxSession pc = MaxSession.createElvInstance();
		pc.login(username, password);
	}

	@Test
	public void getMaxCubeState() {
		if (username == null) return;
		MaxSession session = MaxSession.createElvInstance();
		// MaxSession session = MaxSession.createEq3Instance(new DefaultHttpClient());
		// MaxSession session = MaxSession.createMdInstance(new DefaultHttpClient());
		session.login(username, password);
		MaxCubeState state = session.getMaxCubeState();
		System.out.println(state.toString());
		MaxHouse house = state.getHouse();
		assertNotNull(house);

		List<MaxDevice> houseDevices = house.getDevices();
		assertNotEmpty(houseDevices);
		for (MaxDevice device : houseDevices) {
			System.out.println("  " + device);
		}

		List<MaxRoom> rooms = state.getRooms();
		assertSize(rooms, 9);
		for (MaxRoom room : rooms) {
			System.out.println(room.getName() + ": " + room.getControlMode());
			List<MaxDevice> devices = room.getDevices();
			assertNotEmpty(devices);
			for (MaxDevice device : devices) {
				System.out.println("  " + device);
			}
			MaxWeekTemperatureProfile profile = room.getWeekTemperatureProfile();
			assertEquals(profile.getDayTemperatureProfile(Weekday.MONDAY).getDayOfWeek(), "Monday");
			assertEquals(profile.getDayTemperatureProfile(Weekday.TUESDAY).getDayOfWeek(), "Tuesday");
			assertEquals(profile.getDayTemperatureProfile(Weekday.WEDNESDAY).getDayOfWeek(), "Wednesday");
			assertEquals(profile.getDayTemperatureProfile(Weekday.THURSDAY).getDayOfWeek(), "Thursday");
			assertEquals(profile.getDayTemperatureProfile(Weekday.FRIDAY).getDayOfWeek(), "Friday");
			assertEquals(profile.getDayTemperatureProfile(Weekday.SATURDAY).getDayOfWeek(), "Saturday");
			assertEquals(profile.getDayTemperatureProfile(Weekday.SUNDAY).getDayOfWeek(), "Sunday");
			assertEquals(profile.getDayTemperatureProfileForToday().getDayOfWeek(), Weekday.today().toString());
		}
	}

}
