package ilarkesto.json;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Json {

	public static void printValue(Object value, PrintWriter out, int indentation) {
		if (value == null) {
			out.print("null");
			return;
		}

		if (value instanceof String) {
			out.print('"');
			out.print(escapeString((String) value));
			out.print('"');
			return;
		}

		if (value instanceof Iterable) {
			out.print('[');
			if (indentation >= 0) indentation++;
			boolean indentArray = indentation > 0 && !isShort((Iterable) value);
			Iterable list = (Iterable) value;
			boolean first = true;
			for (Object element : list) {
				if (first) {
					first = false;
				} else {
					out.print(',');
				}
				if (indentArray) {
					out.print('\n');
					indent(out, indentation);
				}
				printValue(element, out, indentation);
			}
			if (indentArray) {
				out.print('\n');
			}
			if (indentation >= 0) indentation--;
			indent(out, indentation);
			out.print(']');
			return;
		}

		if (value instanceof JsonObject) {
			((JsonObject) value).print(out, indentation);
			return;
		}

		if (value instanceof JsonWrapper) {
			((JsonWrapper) value).getJson().print(out, indentation);
			return;
		}

		if (value instanceof BigDecimal) {
			out.print(((BigDecimal) value).toPlainString());
			return;
		}

		if (value instanceof Double) {
			out.print(new BigDecimal(value.toString()).toPlainString());
			return;
		}

		if (value instanceof Float) {
			out.print(new BigDecimal(value.toString()).toPlainString());
			return;
		}

		out.print(value);
	}

	public static String valueToString(Object value, int indentation) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter out = new PrintWriter(stringWriter);
		printValue(value, out, indentation);
		out.flush();
		return stringWriter.toString();
	}

	static boolean isShort(Iterable iterable) {
		int size = 0;
		for (Object element : iterable) {
			if (element == null) continue;
			if (!isPrimitive(element)) return false;
			size += element.toString().length();
			if (size > 80) return false;
		}
		return true;
	}

	static boolean isPrimitive(Object value) {
		if (value == null) return true;
		if (value instanceof String) return true;
		if (value instanceof Number) return true;
		if (value instanceof Boolean) return true;
		return false;
	}

	static void indent(PrintWriter out, int indentation) {
		for (int i = 0; i < indentation; i++) {
			out.print("  ");
		}
	}

	public static String escapeString(String s) {
		if (s == null) return "";
		s = s.replace("\\", "\\\\");
		s = s.replace("\"", "\\\"");
		// s = s.replace("/", "\\/"); seams not to be necessary
		s = s.replace("\b", "\\b");
		s = s.replace("\f", "\\f");
		s = s.replace("\n", "\\n");
		s = s.replace("\r", "\\r");
		s = s.replace("\t", "\\t");
		return s;
	}

	public static String parseString(String s) {
		int idx = s.indexOf("\\u");
		while (idx >= 0) {
			String code = s.substring(idx + 2, idx + 6);
			char ch = (char) Integer.parseInt(code, 16);
			s = s.replace("\\u" + code, String.valueOf(ch));
			idx = s.indexOf("\\u", idx);
		}
		s = s.replace("\\\"", "\"");
		s = s.replace("\\\\", "\\");
		s = s.replace("\\/", "/");
		s = s.replace("\\b", "\b");
		s = s.replace("\\f", "\f");
		s = s.replace("\\n", "\n");
		s = s.replace("\\r", "\r");
		s = s.replace("\\t", "\t");
		return s;
	}

	public static Number parseNumber(String s) throws NumberFormatException {
		if (s.contains(".")) return new BigDecimal(s);
		if (s.contains("E")) return new BigDecimal(new BigDecimal(s).toPlainString());
		return Long.parseLong(s);
	}

	public static boolean isWhitespace(char c) {
		return c == ' ' || c == '\t' || c == '\n' || c == '\r';
	}

	static Object convertValue(Object value) {
		if (value == null) return null;
		if (value instanceof JsonWrapper) return ((JsonWrapper) value).getJson();
		if (value instanceof JsonObject) return value;
		if (value instanceof String) return value;
		if (value instanceof List) return value;
		if (value instanceof Boolean) return value;
		if (value instanceof Number) return value;
		if (value instanceof Collection) return new ArrayList((Collection) value);
		if (value instanceof Iterable) {
			List ret = new ArrayList();
			for (Object element : (Iterable) value) {
				ret.add(element);
			}
			return ret;
		}
		if (value instanceof Map) return new JsonObject((Map) value);
		return value.toString();
	}

	static int getFirstNonWhitespaceIndex(String s, int offset) {
		int len = s.length();
		for (int i = offset; i < len; i++) {
			if (!isWhitespace(s.charAt(i))) return i;
		}
		return -1;
	}

	static int getFirstQuoting(String s, int offset) {
		int len = s.length();
		for (int i = offset; i < len; i++) {
			char ch = s.charAt(i);
			if (ch == '\\') {
				i++;
				continue;
			}
			if (ch == '"') return i;
		}
		return -1;
	}

	public static interface JsonWrapper {

		JsonObject getJson();
	}

}
