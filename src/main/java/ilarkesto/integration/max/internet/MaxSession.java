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
package ilarkesto.integration.max.internet;

import ilarkesto.core.logging.Log;
import ilarkesto.integration.max.state.MaxCubeState;
import ilarkesto.integration.max.state.MaxRoom;
import ilarkesto.json.Json;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * https://www.max-portal.elv.de/dwr/test/MaxRemoteApi
 */
public class MaxSession {

	private static Log log = Log.get(MaxSession.class);

	private String baseUrl;
	private RequestExecutor requestExecutor;

	private int batchId;
	private String httpSessionId;
	private String scriptSessionId;

	private String user;
	private String password;

	private MaxCubeState maxCubeState;

	public MaxSession(String baseUrl) {
		super();
		this.baseUrl = baseUrl;

		if (!this.baseUrl.endsWith("/")) this.baseUrl += "/";

	}

	public static MaxSession createElvInstance() {
		return new MaxSession("https://www.max-portal.elv.de/");
	}

	public static MaxSession createMdInstance() {
		return new MaxSession("https://smarthome.md.de/");
	}

	public static MaxSession createEq3Instance() {
		return new MaxSession("https://max.eq-3.de/");
	}

	public void executeSetRoomAutoMode(MaxRoom room) {
		Map<String, String> extra = new LinkedHashMap<String, String>();
		extra.put("c0-e2", "string:" + room.getId());
		extra.put("c0-e1", "Object_MaxSetRoomAutoMode:{roomId:reference:c0-e2}");
		executeApiMethod(true, "setClientCommands", extra, "Array:[reference:c0-e1]");
		log.info("Command transmitted:", "SetRoomAutoMode", room.getName());
	}

	public void executeSetRoomEcoMode(MaxRoom room) {
		executeSetRoomPermanentMode(room, room.getEcoTemperature());
	}

	public void executeSetRoomComfortMode(MaxRoom room) {
		executeSetRoomPermanentMode(room, room.getComfortTemperature());
	}

	public void executeSetRoomPermanentMode(MaxRoom room, float temperature) {
		Map<String, String> extra = new LinkedHashMap<String, String>();
		extra.put("c0-e2", "string:" + room.getId());
		extra.put("c0-e3", "number:" + temperature);
		extra.put("c0-e1", "Object_MaxSetRoomPermanentMode:{roomId:reference:c0-e2, temperature:reference:c0-e3}");
		executeApiMethod(true, "setClientCommands", extra, "Array:[reference:c0-e1]");
		log.info("Command transmitted:", "SetRoomPermanentMode", temperature, room.getName());
	}

	public void executeSetRoomTemporaryMode(MaxRoom room, float temperature, Date until) {
		if (room == null) throw new IllegalArgumentException("room == null");
		if (until == null) throw new IllegalArgumentException("until == null");

		Calendar cal = Calendar.getInstance();
		cal.setTime(until);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		int minute = cal.get(Calendar.MINUTE);
		if (minute == 30 || minute == 0) {
			// no change
		} else if (minute > 27) {
			cal.roll(Calendar.HOUR_OF_DAY, 1);
			cal.set(Calendar.MINUTE, 0);
		} else {
			cal.set(Calendar.MINUTE, 30);
		}
		until = cal.getTime();

		Map<String, String> extra = new LinkedHashMap<String, String>();
		extra.put("c0-e2", "string:" + room.getId());
		extra.put("c0-e3", "Date:" + until.getTime());
		extra.put("c0-e4", "number:" + temperature);
		extra.put("c0-e1",
			"Object_MaxSetRoomTemporaryMode:{roomId:reference:c0-e2, date:reference:c0-e3, temperature:reference:c0-e4}");
		executeApiMethod(true, "setClientCommands", extra, "Array:[reference:c0-e1]");
		log.info("Command transmitted:", "SetRoomTemporaryMode", temperature, until, room.getName());
	}

	public MaxCubeState getMaxCubeState() {
		String result = executeApiMethod(true, "getMaxCubeState", null);

		DwrParser parser = new DwrParser(result);

		if (!parser.contains("var s0=new MaxCubeState();"))
			throw new MaxProtocolException("Missing 'new MaxCubeState()' in response", result);

		maxCubeState = (MaxCubeState) parser.parseCallbackObject();
		maxCubeState.wire();
		log.info("State loaded");
		return maxCubeState;
	}

	void relogin() {
		login(user, password);
	}

	public void login(String user, String password) throws LoginFailedException {
		initialize();

		String result = executeApiMethod(false, "login", null, "string:" + user, "string:" + password);

		DwrParser parser = new DwrParser(result);
		if (parser.isError()) throw new LoginFailedException(baseUrl, user, parser.getErrorMessage());

		if (!parser.contains("dwr.engine._remoteHandleCallback("))
			throw new MaxProtocolException("Missing callback in response", result);

		this.user = user;
		this.password = password;
	}

	synchronized String executeApiMethod(boolean reloginOnFailure, String name, Map<String, String> extraParams,
			String... arguments) {
		batchId++;

		Map<String, String> parameters = new LinkedHashMap<String, String>();
		parameters.put("callCount", "1");
		parameters.put("page", "/dwr/test/MaxRemoteApi");
		parameters.put("httpSessionId", httpSessionId);
		parameters.put("scriptSessionId", scriptSessionId);
		parameters.put("c0-scriptName", "MaxRemoteApi");
		parameters.put("c0-methodName", name);
		parameters.put("c0-id", "0");
		if (extraParams != null) parameters.putAll(extraParams);
		for (int i = 0; i < arguments.length; i++) {
			parameters.put("c0-param" + i, arguments[i]);
		}
		parameters.put("batchId", String.valueOf(batchId));

		String result = requestExecutor.postAndGetContent(baseUrl + "dwr/call/plaincall/MaxRemoteApi.login.dwr",
			parameters);

		if (result.contains("message=\"Subject is not authenticated\"")) {
			if (user == null || password == null) throw new RuntimeException("Login required");
			relogin();
			return executeApiMethod(false, name, extraParams, arguments);
		}

		httpSessionId = requestExecutor.getSessionId();

		if (result.contains("MaxClientException")) {
			String message = "Command execution failed: " + name;
			int messageIdx = result.indexOf("message=\"");
			if (messageIdx > 0) {
				messageIdx += 9;
				message += " -> " + Json.parseString(result.substring(messageIdx, result.indexOf("\"", messageIdx)));
			}
			if (reloginOnFailure) {
				relogin();
				return executeApiMethod(false, name, extraParams, arguments);
			} else {
				throw new MaxCommandFailedException(message);
			}
		}

		if (!result.contains("dwr.engine._remoteHandleCallback('" + batchId + "'")) {
			if (reloginOnFailure) {
				relogin();
				return executeApiMethod(false, name, extraParams, arguments);
			} else {
				throw new MaxCommandFailedException("Command execution failed: " + name + ". Unexpected result: "
						+ result);
			}
		}
		return result;
	}

	void initialize() {
		requestExecutor = new RequestExecutor();
		batchId = 0;

		String engineScriptUrl = baseUrl + "dwr/engine.js";

		String script = requestExecutor.get(engineScriptUrl);

		httpSessionId = requestExecutor.getSessionId();

		DwrParser parser = new DwrParser(script);
		if (!parser.gotoAfterIf("dwr.engine._origScriptSessionId = \""))
			throw new MaxProtocolException("Missing 'dwr.engine._origScriptSessionId' in " + engineScriptUrl, script);
		String origScriptSessionId = parser.getUntilIf("\"");
		if (origScriptSessionId == null)
			throw new MaxProtocolException("Missing 'dwr.engine._origScriptSessionId = \"...\"' in " + engineScriptUrl,
					script);
		scriptSessionId = origScriptSessionId + Math.floor(Math.random() * 1000);
	}

	public MaxCubeState getLastMaxCubeState() {
		return maxCubeState;
	}

	public String getScriptSessionId() {
		return scriptSessionId;
	}

	public String getHttpSessionId() {
		return httpSessionId;
	}

	public int getBatchId() {
		return batchId;
	}

}
