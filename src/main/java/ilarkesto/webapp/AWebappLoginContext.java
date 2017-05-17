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
package ilarkesto.webapp;

import ilarkesto.auth.LoginContext;
import ilarkesto.core.logging.Log;

public abstract class AWebappLoginContext<U, S extends AWebSession> implements LoginContext<U> {

	public static final String COOKIE_AUTH_KEY = "auth";
	public static final String REQUEST_PARAMETER_USERNAME = "username";
	public static final String REQUEST_PARAMETER_PASSWORD = "password";

	protected final Log log = Log.get(getClass());
	protected RequestWrapper<S> req;

	protected abstract void afterLoginSuccess(U user);

	protected abstract void putUserInSession(U user, S session);

	public AWebappLoginContext(RequestWrapper<S> req) {
		this.req = req;
	}

	@Override
	public final String getProvidedAuthrorizationSecret() {
		return req.getCookie(COOKIE_AUTH_KEY);
	}

	@Override
	public final String getProvidedUsername() {
		return req.get(REQUEST_PARAMETER_USERNAME);
	}

	@Override
	public final String getProvidedPassword() {
		return req.get(REQUEST_PARAMETER_PASSWORD);
	}

	@Override
	public final void loginSuccess(U user) {
		putUserInSession(user, req.getSession());
		String authKey = getAuthorizationSecret(user);
		if (authKey != null) req.setCookie(COOKIE_AUTH_KEY, authKey, getAuthKeyCookieTtl());
		afterLoginSuccess(user);
	}

	protected int getAuthKeyCookieTtl() {
		return 60 * 60 * 24 * 30;
	}

}
