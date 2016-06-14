package ilarkesto.gwt.client;

import ilarkesto.core.base.Str;
import ilarkesto.core.base.Utl;

import java.io.Serializable;
import java.util.Collection;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ErrorWrapper implements Serializable, IsSerializable {

	public static final String SERVER_NOT_AVAILABLE = "ServerNotAvailable";
	public static final String USER_INPUT = "UserInput";
	public static final String NO_PERMISSION = "NoPermission";

	private String name;
	private String message;

	public static ErrorWrapper createNoPermission() {
		return new ErrorWrapper(NO_PERMISSION, "No permission.");
	}

	public static ErrorWrapper createServerNotAvailable() {
		return new ErrorWrapper(SERVER_NOT_AVAILABLE, "Server not available.");
	}

	public static ErrorWrapper createUserInput(String message) {
		return new ErrorWrapper(USER_INPUT, message);
	}

	public ErrorWrapper(String name, String message) {
		super();
		this.name = name;
		this.message = message;
	}

	public ErrorWrapper(Throwable ex) {
		this(ex.getClass().getName(), Str.formatException(ex));
	}

	private ErrorWrapper() {
		this("unknown error", null);
	}

	public boolean isLoginRequired() {
		return message.contains("Login required");
	}

	public boolean isServerNotAvailable() {
		return SERVER_NOT_AVAILABLE.equals(name);
	}

	public boolean isUserInput() {
		return USER_INPUT.equals(name);
	}

	public String getName() {
		return name;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		if (message == null) return name;
		return message;
	}

	@Override
	public int hashCode() {
		int hash = name.hashCode();
		if (message != null) hash = hash * message.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ErrorWrapper)) return false;
		ErrorWrapper other = (ErrorWrapper) obj;
		return name.equals(other.name) && Utl.equals(message, other.message);
	}

	public static String toString(Collection<ErrorWrapper> errors) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (ErrorWrapper error : errors) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(error);
		}
		return sb.toString();
	}

}
