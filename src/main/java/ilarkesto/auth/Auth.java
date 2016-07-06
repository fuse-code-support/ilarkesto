/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ilarkesto.auth;

import ilarkesto.base.Str;
import ilarkesto.base.Utl;
import ilarkesto.core.base.UserInputException;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.Tm;
import ilarkesto.persistence.AEntity;
import ilarkesto.text.TextGenerator;

import java.util.Collection;
import java.util.Map;

public class Auth {

	private static final Log log = Log.get(Auth.class);

	public static <U> void changePasswordWithCheck(String currentPassword, String newPassword, U user,
			AuthenticationContext<U> context) throws UserInputException {
		if (!Auth.isPasswordMatching(currentPassword, user, context)) throw new WrongPasswordInputException();

		setPassword(newPassword, user, context);
	}

	public static <U> boolean isPasswordMatchingDefaultPassword(U user, AuthenticationContext<U> context) {
		return isPasswordMatching(context.getDefaultPassword(user), user, context);
	}

	public static <U> boolean isPasswordMatching(String passwordToCheck, U user, AuthenticationContext<U> context) {
		String userPasswordHash = context.getPasswordHash(user);
		String userPasswordSalt = context.getPasswordSalt(user);
		return isPasswordMatching(passwordToCheck, userPasswordHash, userPasswordSalt);
	}

	public static <U> void resetPasswordToDefault(U user, AuthenticationContext<U> context) {
		String defaultPassword = context.getDefaultPassword(user);
		if (Str.isBlank(defaultPassword)) throw new IllegalStateException("Blank default password.");
		setPasswordWithoutChecking(user, defaultPassword, context);
	}

	public static <U> void setPassword(String password, U user, AuthenticationContext<U> context)
			throws UserInputException {
		String veto = context.getNewPasswordVeto(user, password);
		if (veto != null) throw new UserInputException(veto);

		veto = getPasswordVeto(password, context.getUsersKnownStrings(user));
		if (veto != null) throw new UserInputException(veto);

		setPasswordWithoutChecking(user, password, context);
	}

	private static <U> void setPasswordWithoutChecking(U user, String password, AuthenticationContext<U> context) {
		String salt = TextGenerator.password(256);
		context.setPasswordSalt(user, salt);
		context.setPasswordHash(user, hashPassword(salt, password));
		context.passwordChanged(user);
	}

	public static String getPasswordVeto(String password, Collection<String> knownStrings) {
		if (Str.isBlank(password)) return "Must not be empty";
		if (password.length() < 6) return "Requires at least 6 characters";
		if (knownStrings != null) {
			String pw = password.trim().toLowerCase();
			for (String knownString : knownStrings) {
				if (Str.isBlank(knownString)) continue;
				String ks = knownString.trim().toLowerCase();
				if (ks.contains(pw) || pw.contains(ks))
					return "Must differ known information (like name, address, etc.)";
			}
		}
		return null;
	}

	public static <U> void tryLogin(LoginContext<U> loginContext) {
		Utl.sleep(Tm.SECOND);

		U user = null;

		if (user == null) user = checkPassword(loginContext);

		if (user == null) user = checkAuthKey(loginContext);

		if (user == null) return;

		if (!loginContext.isUserAllowedToLogin(user)) return;

		loginContext.loginSuccess(user);
	}

	private static <U> U checkAuthKey(LoginContext<U> loginContext) {
		String authKey = loginContext.getProvidedAuthrorizationSecret();
		if (Str.isBlank(authKey)) return null;

		return loginContext.getUserByAuthorizationSecret(authKey);

	}

	private static <U> U checkPassword(LoginContext<U> loginContext) {
		String username = loginContext.getProvidedUsername();
		if (Str.isBlank(username)) return null;

		U user = loginContext.getUserByUsername(username);
		if (user == null) return null;

		String passwordToCheck = loginContext.getProvidedPassword();
		String userPasswordHash = loginContext.getPasswordHash(user);
		String userPasswordSalt = loginContext.getPasswordSalt(user);

		if (!isPasswordMatching(passwordToCheck, userPasswordHash, userPasswordSalt)) return null;

		return user;
	}

	public static boolean isPasswordMatching(String passwordToCheck, String userPasswordHash, String userPasswordSalt) {
		if (Str.isBlank(passwordToCheck)) return false;
		if (Str.isBlank(userPasswordHash)) return false;
		if (Str.isBlank(userPasswordSalt)) return false;

		String hashToCheck = hashPassword(userPasswordSalt, passwordToCheck);

		return userPasswordHash.equals(hashToCheck);
	}

	private static String hashPassword(String userPasswordSalt, String password) {
		return PasswordHasher.hashPasswordWithSha256(password, userPasswordSalt, "SHA-256:");
	}

	public static boolean isVisible(Object entity, AuthUser user) {
		if (entity instanceof ViewProtected) return ((ViewProtected) entity).isVisibleFor(user);
		if (entity instanceof Ownable) return ((Ownable) entity).isOwner(user);
		return true;
	}

	public static boolean isEditable(Object entity, AuthUser user) {
		if (entity instanceof EditProtected) return ((EditProtected) entity).isEditableBy(user);
		return isVisible(entity, user);
	}

	public static boolean isDeletable(Object entity, AuthUser user) {
		if (entity instanceof DeleteProtected) return ((DeleteProtected) entity).isDeletableBy(user);
		return isEditable(entity, user);
	}

	private Auth() {}

	public static boolean isEntityEditable(AuthUser user, AEntity entity, Map<String, String> properties) {
		return isEditable(entity, user);
	}

	public static boolean isEntityDeletable(AuthUser user, AEntity entity) {
		return isDeletable(entity, user);
	}

}
