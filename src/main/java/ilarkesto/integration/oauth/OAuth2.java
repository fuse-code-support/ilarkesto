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
package ilarkesto.integration.oauth;

import ilarkesto.core.auth.LoginDataProvider;
import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.json.JsonObject;
import ilarkesto.net.httpclient.HttpSession;

import java.util.HashMap;

public class OAuth2 {

	private final Log log = Log.get(getClass());

	public static final String REDIRECT_OOB = "urn:ietf:wg:oauth:2.0:oob";

	private String authEndpoint;
	private String tokenEndpoint;
	private String clientId;
	private String clientSecret;
	private String redirectUri;
	private String scope;
	private String refreshToken;

	private String accessToken;

	public OAuth2(String authEndpoint, String tokenEndpoint, LoginDataProvider clientIdAndSecret, String redirectUri,
			String refreshToken, String scope) {
		this(authEndpoint, tokenEndpoint, clientIdAndSecret.getLoginData().getLogin(),
				clientIdAndSecret.getLoginData().getPassword(), redirectUri, refreshToken, scope);
	}

	public OAuth2(String authEndpoint, String tokenEndpoint, String clientId, String clientSecret, String redirectUri,
			String refreshToken, String scope) {
		super();
		this.authEndpoint = authEndpoint;
		this.tokenEndpoint = tokenEndpoint;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.redirectUri = redirectUri;
		this.refreshToken = refreshToken;
		this.scope = scope;
	}

	public String createUrlForAuthenticationRequest(boolean forceApprovalPrompt) {
		StringBuilder sb = new StringBuilder();
		sb.append(authEndpoint).append("?");
		sb.append("scope=").append(Str.encodeUrlParameter(scope)).append("&");
		sb.append("redirect_uri=").append(Str.encodeUrlParameter(redirectUri)).append("&");
		sb.append("client_id=").append(clientId).append("&");
		sb.append("response_type=code&");
		sb.append("access_type=offline&");
		sb.append("include_granted_scopes=true");
		if (forceApprovalPrompt) sb.append("&approval_prompt=force");
		String url = sb.toString();
		return url;
	}

	public void exchangeRefreshTokenForAccessToken() {
		if (Str.isBlank(refreshToken)) throw new IllegalArgumentException("refresh token not set");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("refresh_token", refreshToken);
		params.put("client_id", clientId);
		params.put("client_secret", clientSecret);
		params.put("grant_type", "refresh_token");

		String result = new HttpSession().postAndDownloadText(tokenEndpoint, params);

		JsonObject json = JsonObject.parse(result);

		accessToken = json.getString("access_token");
		if (Str.isBlank(accessToken)) throw new RuntimeException(
				"OAuth: Exchanging refresh token for access token failed. Missing access_token: " + result);

		log.info("Refresh token exchanged for access token");
	}

	public void exchangeAuthorizationCodeForAccessToken(String authorizationCode) {
		if (Str.isBlank(authorizationCode)) throw new IllegalArgumentException("authorizationCode required");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("code", authorizationCode);
		params.put("client_id", clientId);
		params.put("client_secret", clientSecret);
		params.put("grant_type", "authorization_code");
		params.put("redirect_uri", redirectUri);
		params.put("access_type", "offline");
		String result = new HttpSession().postAndDownloadText(tokenEndpoint, params);

		JsonObject json = JsonObject.parse(result);

		accessToken = json.getString("access_token");
		if (Str.isBlank(accessToken)) throw new RuntimeException(
				"OAuth: Exchanging authorization code for access+refresh token failed. Missing access_token: "
						+ result);

		refreshToken = json.getString("refresh_token");
		if (Str.isBlank(refreshToken)) throw new RuntimeException(
				"OAuth: Exchanging authorization code for access/refresh token failed. Missing refresh_token: "
						+ result);

		log.info("Authorization code exchanged for access token and refresh token");
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public String getAccessToken(boolean autoRefresh) {
		if (accessToken == null && autoRefresh) exchangeRefreshTokenForAccessToken();
		return accessToken;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getScope() {
		return scope;
	}

}
