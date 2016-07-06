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
package ilarkesto.base;

import ilarkesto.integration.links.LinkConverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Utilitiy methods for dealing with text/strings. Cutting, comparing, parsing, modifying.
 */
public class Str extends ilarkesto.core.base.Str {

	public static void main(String[] args) {
		System.out.println(getSimilarity("St. Märgen", "Sankt Märgen"));
	}

	private static final char[] UNICODE_CHARS = new char[] { ue, UE, oe, OE, ae, AE, sz, EUR };

	private static final String[][] ESCAPE_SEQUENCES = { { "\\", "\\\\" }, { "\b", "\\b" }, { "\t", "\\t" },
			{ "\n", "\\n" }, { "\f", "\\f" }, { "\r", "\\r" }, { "\"", "\\\"" }, { "\'", "\\\'" } };

	private static long lastId;

	private static final Object UIDLOCK = new Object();

	public static List<String> tokenize(String s, String delimiters) {
		if (s == null) return Collections.emptyList();
		List<String> ret = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(s, delimiters);
		while (tokenizer.hasMoreTokens()) {
			ret.add(tokenizer.nextToken());
		}
		return ret;
	}

	public static String getCharsetFromHtml(String html, String defaultCharset) {
		String charset = Str.cutFromTo(html, "charset=", "\"");
		if (Str.isBlank(charset)) charset = defaultCharset;
		return charset;
	}

	public static String[] tokenizeToArray(String s, String delimiter) {
		StringTokenizer tok = new StringTokenizer(s, delimiter);
		LinkedList<String> ll = new LinkedList<String>();
		while (tok.hasMoreTokens()) {
			ll.add(tok.nextToken());
		}
		return toStringArray(ll);
	}

	public static String multiply(String s, int factor) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < factor; i++) {
			sb.append(s);
		}
		return sb.toString();
	}

	public static boolean endsWith(String s, String... suffixes) {
		for (String suffix : suffixes) {
			if (s.endsWith(suffix)) return true;
		}
		return false;
	}

	public static String toHexString(byte[] bytes) {
		if (bytes == null) return null;
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(toHexString(b));
		}
		return sb.toString();
	}

	public static String toHexString(byte b) {
		int i = b;
		if (i < 0) i = 256 + i;
		String s = Integer.toHexString(i).toUpperCase();
		if (s.length() == 1) s = '0' + s;
		return s;
	}

	public static String toBinaryString(byte b) {
		int i = b;
		if (i < 0) i = 256 + i;
		String s = Integer.toBinaryString(i).toUpperCase();
		if (s.length() == 1) s = '0' + s;
		return s;
	}

	public static String substringTo(String s, String to) {
		return substringTo(s, to, 0);
	}

	public static String substringTo(String s, String to, int fromIndex) {
		if (s == null) return null;
		if (to == null) return s;
		int idx = s.indexOf(to, fromIndex);
		if (idx < 0) return s;
		return s.substring(fromIndex, idx);
	}

	public static String removeUnreadableChars(String s) {
		if (s == null) return null;
		StringBuilder sb = new StringBuilder();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (isReadable(c)) sb.append(c);
		}
		return sb.toString();
	}

	public static String removeFilenameSuffix(String filename) {
		if (filename == null) return null;
		int idx = filename.lastIndexOf('.');
		if (idx < 0) return filename;
		return filename.substring(0, idx);
	}

	public static boolean isReadable(char c) {
		if (Character.isLetterOrDigit(c)) return true;
		if (c == ' ' || c == '\n' || c == '!' || c == '"' || c == '§' || c == '$' || c == '%' || c == '&' || c == '/'
				|| c == '(' || c == ')' || c == '=' || c == '?' || c == '{' || c == '}' || c == '[' || c == ']'
				|| c == '\\' || c == '*' || c == '+' || c == '~' || c == '#' || c == '\'' || c == '-' || c == '_'
				|| c == '.' || c == ':' || c == ',' || c == ';' || c == '<' || c == '>' || c == '@' || c == EUR
				|| c == '^' || c == '|' || c == 'µ' || c == '²' || c == '³')
			return true;
		return false;
	}

	public static String replaceUnicodeCharsWithJavaNotation(String s) {
		s = s.replace(String.valueOf(ue), "\\u00FC");
		s = s.replace(String.valueOf(UE), "\\u00DC");
		s = s.replace(String.valueOf(oe), "\\u00F6");
		s = s.replace(String.valueOf(OE), "\\u00D6");
		s = s.replace(String.valueOf(ae), "\\u00E4");
		s = s.replace(String.valueOf(AE), "\\u00C4");
		s = s.replace(String.valueOf(sz), "\\u00DF");
		s = s.replace(String.valueOf(EUR), "\\u0080");
		return s;
	}

	public static boolean containsUnicodeChar(String s) {
		return containsChar(s, UNICODE_CHARS);
	}

	public static boolean containsChar(String s, char... chars) {
		for (char c : chars) {
			if (s.indexOf(c) >= 0) return true;
		}
		return false;
	}

	public static String activateLinksInHtml(String html, LinkConverter linkConverter) {
		return activateLinksInHtml(html, linkConverter, 640);
	}

	public static String activateLinksInHtml(String s, LinkConverter linkConverter, int maxWidth) {
		if (s == null) return null;
		int fromIndex = 0;
		StringBuffer result = null;
		int idx = -1;
		while ((idx = firstIndexOf(s, fromIndex, "http://", "https://", "ftp://", "www.")) >= 0) {
			char pre = idx == 0 ? ' ' : s.charAt(idx - 1);
			if (pre == ' ' || pre == '\n' || pre == '>' || pre == ';') {
				int endIdx = firstIndexOf(s, idx, " ", "<", "\n", pre == ';' ? "&gt;" : "\n");
				if (endIdx <= 0 || !s.substring(endIdx).startsWith("</a>")) {
					if (endIdx < 0) endIdx = s.length();
					// activate
					String url = s.substring(idx, endIdx);
					if (result == null) result = new StringBuffer();
					result.append(s.substring(fromIndex, idx));
					result.append("<a href=\"");
					result.append(url.startsWith("www.") ? "http://" + url : url);
					result.append("\" target=\"_blank\">");

					String convertedUrl = linkConverter == null ? url : linkConverter.convert(url, maxWidth);
					if (url.equals(convertedUrl)) {
						String label = url;
						if (url.startsWith("http://")) url = url.substring(7);
						if (url.startsWith("https://")) url = url.substring(8);
						if (url.startsWith("www.")) url = url.substring(4);
						label = cutRight(url, 30, "...");
						result.append(label);
					} else {
						result.append(convertedUrl);
					}

					result.append("</a>");
					fromIndex = endIdx;
					continue;
				}
			}
			// nothing to activate
			if (result == null) result = new StringBuffer();
			result.append(s.substring(fromIndex, idx));
			result.append(s.charAt(idx));
			fromIndex = idx + 1;
		}
		if (result == null) return s;
		result.append(s.substring(fromIndex));
		return result.toString();
	}

	public static int firstIndexOf(String s, int fromIndex, String... stringsToFind) {
		int idx = Integer.MAX_VALUE;
		for (String find : stringsToFind) {
			int i = s.indexOf(find, fromIndex);
			if (i >= 0) idx = Math.min(idx, i);
		}
		idx = idx == Integer.MAX_VALUE ? -1 : idx;
		return idx;
	}

	public static boolean isUpperCase(String s) {
		int len = s.length();
		for (int i = 0; i < len; i++) {
			if (!Character.isUpperCase(s.charAt(i))) return false;
		}
		return true;
	}

	public static String getFirstTokenAfter(String stringToTokenize, String delimiter, String s) {
		return getTokenAfter(stringToTokenize, delimiter, s, 0);
	}

	public static String getTokenAfter(String stringToTokenize, String delimiter, String s, int index) {
		return getStringAfter(tokenizeToArray(stringToTokenize, delimiter), s, index);
	}

	public static String getFirstStringAfter(String[] strings, String s) {
		return getStringAfter(strings, s, 0);
	}

	/**
	 * Determine the string, which is at a specified position after a specified string.
	 */
	public static String getStringAfter(String[] strings, String s, int index) {
		int i = indexOfStringInArray(s, strings);
		if (i < 0) return null;
		index += i + 1;
		if (index >= s.length()) return null;
		return strings[index];
	}

	/**
	 * Determine the index of a string inside of an string array.
	 */
	public static int indexOfStringInArray(String s, String[] strings) {
		for (int i = 0; i < strings.length; i++) {
			if (equals(strings[i], s)) return i;
		}
		return -1;
	}

	public static boolean equals(String s1, String s2) {
		if (s1 == null && s2 == null) return true;
		if (s1 == null && s2 != null) return false;
		if (s1 != null && s2 == null) return false;
		return s1.equals(s2);
	}

	public static String decodeQuotedPrintable(String s) {
		if (s == null) return null;

		int start = s.indexOf("=?");
		if (start < 0) return s;

		String prefix = s.substring(0, start);

		start = s.indexOf("?Q?", start);
		if (start < 0) start = s.indexOf("?q?", start);
		if (start < 0) start = s.indexOf("?B?", start);
		if (start < 0) start = s.indexOf("?b?", start);
		if (start < 0) return s;
		start += 3;

		int end = s.indexOf("?=", start);
		if (end < 0) return s;

		String suffix = s.substring(end + 2);
		s = s.substring(start, end);

		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		int len = s.length();
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (c == '=') {
				if (i + 1 == len) {
					sb.append('=');
					break;
				}
				char next = s.charAt(i + 1);
				if (next == 13 || next == 10) {
					i++;
					next = s.charAt(i + 1);
					if (next == 13 || next == 10) {
						i++;
					}
					continue;
				}
				if (next == '=') {
					sb.append('=');
					i++;
					continue;
				}
				char afterNext = s.charAt(i + 2);
				int chi = Integer.valueOf(String.valueOf(next) + afterNext, 16);
				char ch = (char) chi;
				if (!Character.isDefined(chi) || Character.isHighSurrogate(ch) || Character.isISOControl(chi)
						|| Character.isSupplementaryCodePoint(chi)) {
					sb.append(' ');
				} else {
					sb.append(chi);
				}
				i += 2;
			} else if (c == '_') {
				sb.append(' ');
			} else {
				sb.append(c);
			}
		}
		sb.append(suffix);
		return sb.toString();
	}

	public static boolean isFilenameCompatible(String s) {
		for (int i = 0, n = s.length(); i < n; i++) {
			char c = s.charAt(i);
			if (!(Character.isDigit(c) || Character.isLetter(c) || c == '_' || c == '-' || c == '.')) return false;
		}
		return true;
	}

	public static String concatWithUppercaseFirstLetter(String[] sa, boolean ignoreFirst) {
		StringBuilder sb = new StringBuilder();
		if (ignoreFirst) {
			sb.append(sa[0]);
		}
		for (int i = ignoreFirst ? 1 : 0; i < sa.length; i++) {
			sb.append(Character.toUpperCase(sa[i].charAt(0)));
			sb.append(sa[i].substring(1));
		}
		return sb.toString();
	}

	public static String formatPostalcode(String s) {
		return formatNumber(s);
	}

	public static String formatNumber(String s) {
		if (s == null) return null;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isDigit(c)) sb.append(c);
		}
		return sb.toString();
	}

	public static String formatTelephone(String telnum) {
		if (telnum == null) return null;
		StringBuilder sb = new StringBuilder();
		boolean lastWasSpace = false;
		for (int i = 0; i < telnum.length(); i++) {
			char c = telnum.charAt(i);
			if (Character.isDigit(c) || c == '+') {
				sb.append(c);
				lastWasSpace = false;
			} else {
				if (!lastWasSpace) {
					sb.append(' ');
					lastWasSpace = true;
				}
			}
		}
		telnum = sb.toString().trim();
		if (telnum.startsWith("0") && telnum.length() > 1) telnum = "+49 " + telnum.substring(1);
		return telnum;
	}

	public static String[] tokenizeWithLongDelimiter(String s, String delimiter) {
		List<String> sl = new ArrayList<String>();
		int idx = s.indexOf(delimiter);
		int offset = 0;
		while ((idx = s.indexOf(delimiter, offset)) >= 0) {
			sl.add(s.substring(offset, idx));
			offset = idx + delimiter.length();
		}
		sl.add(s.substring(offset));
		return toStringArray(sl);
	}

	public static char getFirstNonexistingChar(String s) {
		return getFirstNonexistingChar(s, (char) 0);
	}

	public static char getFirstNonexistingChar(String s, char offset) {
		for (char c = offset; c < Character.MAX_VALUE; c++) {
			if (s.indexOf(c) < 0) return c;
		}
		return Character.MAX_VALUE;
	}

	public static String getPrimitiveTypeName(String className) {
		if (className.startsWith("java.lang.")) className = className.substring(10);
		if (className.equals("Long")) className = "long";
		if (className.equals("Integer")) className = "int";
		if (className.equals("Double")) className = "double";
		if (className.equals("Boolean")) className = "boolean";
		if (className.equals("Byte")) className = "byte";
		return className;
	}

	public static List<String> listRelativeFiles(String root, boolean recurse, boolean replaceToUnixSlash,
			boolean ignoreDirs, FileFilter filter) {
		List<String> sl = new ArrayList<String>();
		listRelateveFiles(sl, root, "", recurse, replaceToUnixSlash, ignoreDirs, true, filter);
		return sl;
	}

	public static void listRelateveFiles(List<String> container, String root, String prefix, boolean recurse,
			boolean replaceToUnixSlash, boolean ignoreDirs, boolean allowDuplicates, FileFilter filter) {

		if (replaceToUnixSlash) root = root.replace("\\", "/");

		File rootFile = new File(root);
		File[] files = rootFile.listFiles();
		if (files == null || files.length == 0) return;

		for (int i = 0; i < files.length; i++) {
			if (filter != null && !filter.accept(files[i])) continue;
			boolean dir = files[i].isDirectory();
			if (!dir || !ignoreDirs) {
				String s = prefix + files[i].getName();
				if (allowDuplicates || !container.contains(s)) {
					container.add(s);
				}
			}
			if (dir && recurse) {
				listRelateveFiles(container, root + "/" + files[i].getName(), prefix + files[i].getName() + "/",
					recurse, replaceToUnixSlash, ignoreDirs, allowDuplicates, filter);
			}
		}
	}

	public static boolean isLetterOrDigit(String s) {
		for (int i = s.length() - 1; i >= 0; i--) {
			if (!Character.isLetterOrDigit(s.charAt(i))) return false;
		}
		return true;
	}

	public static boolean isLetter(String s) {
		for (int i = s.length() - 1; i >= 0; i--) {
			if (!Character.isLetter(s.charAt(i))) return false;
		}
		return true;
	}

	public static boolean isMatchingKey(String s, String key) {
		if (s != null) {
			return s.toLowerCase().indexOf(key) >= 0;
		} else {
			return false;
		}
	}

	public static boolean isDigit(String s) {
		for (int i = s.length() - 1; i >= 0; i--) {
			if (!Character.isDigit(s.charAt(i))) return false;
		}
		return true;
	}

	public static String getEnclosed(String s, String opener, String closer) {
		return (new EncloseParser(s, opener, closer)).getEnclosed();
	}

	public static class EncloseParser {

		private String opener;

		private String closer;

		private String s;

		private String prefix;

		private String enclosed;

		private String postfix;

		public EncloseParser(String s, String opener, String closer) {
			this.s = s;
			this.opener = opener;
			this.closer = closer;

			parse();
		}

		private void parse() {
			int idx = s.indexOf(opener);
			if (idx < 0) return;
			prefix = s.substring(0, idx);
			idx += opener.length();

			int stack = 1;
			StringBuilder sb = new StringBuilder();
			int startIdx = idx;
			String[] openClose = new String[] { opener, closer };
			int maxLen = Math.min(opener.length(), closer.length());

			while (stack > 0) {
				if (startIdx == s.length() - 1) return;
				idx = indexOf(s, openClose, startIdx);
				if (idx < 0) return;
				String rest = s.substring(idx, idx + maxLen);
				if (rest.startsWith(opener)) {
					stack++;
					sb.append(s.substring(startIdx, (startIdx = idx + opener.length())));
				} else {
					stack--;
					sb.append(s.substring(startIdx, idx));
					if (stack != 0) sb.append(closer);
					startIdx = idx + closer.length();
				}
			}
			postfix = s.substring(startIdx);

			enclosed = sb.toString();

		}

		public String getEnclosed() {
			return enclosed;
		}

		public String getPostfix() {
			return postfix;
		}

		public String getPrefix() {
			return prefix;
		}
	}

	public static String replaceForSql(String s) {
		s = s.replace("\\", "\\\\");
		s = s.replace("\"", "\\\"");
		return s;
	}

	public static String[] remove(int index, String[] elements) {
		String[] sa = new String[elements.length - 1];
		System.arraycopy(elements, 0, sa, 0, index);
		System.arraycopy(elements, index + 1, sa, index, sa.length - index);
		return sa;
	}

	public static String[] remove(String elementToRemove, String[] elements) {
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].equals(elementToRemove)) { return remove(i, elements); }
		}
		return elements;
	}

	public static String appendIfMarkerNotExists(String s, String marker, String textToAppend) {
		if (s == null) return marker + textToAppend;
		if (s.indexOf(marker) >= 0) return s;
		return s + marker + textToAppend;
	}

	public static String[] append(String newElement, String[] elements) {
		String[] sa = new String[elements.length + 1];
		System.arraycopy(elements, 0, sa, 0, elements.length);
		sa[elements.length] = newElement;
		return sa;
	}

	public static boolean contains(String textToLookFor, String[] textsToLookIn) {
		for (int i = 0; i < textsToLookIn.length; i++) {
			if (textsToLookIn[i].equals(textToLookFor)) return true;
		}
		return false;
	}

	public static long generateUID(long idTimeSub) {
		synchronized (UIDLOCK) {
			long id = Tm.getCurrentTimeMillis() - idTimeSub;
			while (id <= lastId)
				id++;
			lastId = id;
			// LOG.fine("UID generated: "+id);
			return id;
		}
	}

	public static long generateUID() {
		return generateUID(0);
	}

	public static boolean isVersion1LowerThenVersion2(String version1, String version2) {
		if (version1 == null) return true;
		if (version2 == null) return false;
		return parseVersion(version1) < parseVersion(version2);
	}

	public static long parseVersion(String s) {
		long v = 0;
		int factor = 100 * 100 * 100 * 100;
		StringTokenizer tokenizer = new StringTokenizer(s, ".");
		while (tokenizer.hasMoreTokens()) {
			int i = Integer.parseInt(tokenizer.nextToken());
			v += i * factor;
			factor = factor / 100;
		}
		return v;
	}

	public static String[] toLowerCase(String[] value) {
		String[] ret = new String[value.length];
		for (int i = 0; i < value.length; i++) {
			ret[i] = value[i].toLowerCase();
		}
		return ret;
	}

	public static String toString(String message, String key, Object value) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(key, value);
		return toString(message, map);
	}

	public static String toString(String message, String key1, Object value1, String key2, Object value2) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(key1, value1);
		map.put(key2, value2);
		return toString(message, map);
	}

	public static String toString(String message, String key1, Object value1, String key2, Object value2, String key3,
			Object value3) {
		HashMap<String, Object> map = new HashMap<String, Object>(); // TODO orderedMap
		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		return toString(message, map);
	}

	public static String toString(String message, Map<String, Object> map) {
		StringBuilder sb = new StringBuilder();
		sb.append(message);
		for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			sb.append("\n  ").append(entry.getKey()).append(" = ").append(format(entry.getValue()));
		}
		return sb.toString();
	}

	public static String getStackTrace() {
		return getStackTrace(new Exception());
	}

	public static Collection<Object[]> toCollection(Object[] oa) {
		ArrayList<Object[]> al = new ArrayList<Object[]>(oa.length);
		for (int i = 0; i < oa.length; i++) {
			al.add(oa);
		}
		return al;
	}

	public static String[] merge(String[][] saa) {
		LinkedList<String> ll = new LinkedList<String>();
		for (int x = 0; x < saa.length; x++) {
			String[] sa = saa[x];
			for (int y = 0; y <= sa.length; y++) {
				ll.add(sa[y]);
			}
		}
		return toStringArray(ll);
	}

	// public static String[] subarray(String[] sa, int length) {
	// StringList sl = new StringList(sa);
	// while (sl.size() > length)
	// sl.remove(sl.size() - 1);
	// return sl.toStringArray();
	// }

	public static String[] subarray(String[] sa, int beginIndex, int length) {
		String[] result = new String[length];
		System.arraycopy(sa, beginIndex, result, 0, length);
		return result;
	}

	public static String[] subarray(String[] sa, int beginIndex) {
		return subarray(sa, beginIndex, sa.length - beginIndex);
	}

	public static boolean equals(String[] sa1, String[] sa2) {
		if (sa1.length != sa2.length) return false;
		for (int i = 0; i < sa1.length; i++) {
			if (!sa1[i].equals(sa2[i])) return false;
		}
		return true;
	}

	public static List<String> tokenizeString(String s) {
		if (s == null) return Collections.emptyList();
		StringTokenizer tok = new StringTokenizer(s);
		LinkedList<String> ll = new LinkedList<String>();
		while (tok.hasMoreTokens()) {
			ll.add(tok.nextToken());
		}
		return ll;
	}

	public static String[] tokenize(String s) {
		return toStringArray(tokenizeString(s));
	}

	public static String getLastToken(String s, String delimiter) {
		String result = null;
		StringTokenizer tokenizer = new StringTokenizer(s, delimiter);
		while (tokenizer.hasMoreTokens())
			result = tokenizer.nextToken();
		return result;
	}

	/**
	 * @deprecated Use <code>toHtml()</code>
	 */
	@Deprecated
	public static String replaceForHtml(String s) {
		return toHtml(s);
	}

	public static String convertLinksToHtml(String text, String target) {
		StringBuilder sb = new StringBuilder();
		int startIdx = 0;
		int idx;
		while ((idx = indexOf(text, new String[] { "http://", "www.", "ftp://" }, startIdx)) >= 0) {
			int end = indexOf(text, new String[] { " ", "\n", ",", ";" }, idx);
			if (end < 0) end = text.length();
			String link = text.substring(idx, end);
			sb.append(text.substring(startIdx, idx));
			sb.append("<A href=\"").append(link).append("\"");
			if (target != null) {
				sb.append(" target=\"").append(target).append("\"");
			}
			sb.append(">").append(link).append("</A>");
			startIdx = end;
		}
		sb.append(text.substring(startIdx));
		return sb.toString();
	}

	public static String insertIfNotExisting(String into, int index, String insert) {
		StringBuilder sb = new StringBuilder();

		while (into.length() > index) {
			String sub = into.substring(0, index);
			sb.append(sub);
			if (sub.indexOf(insert) < 0) sb.append(insert);
			into = into.substring(index);
		}
		sb.append(into);

		return sb.toString();
	}

	public static ArrayList<String> splitWordLineToList(String line, int maxlen) {
		ArrayList<String> al = new ArrayList<String>();

		while (line.length() > maxlen) {
			int idx = line.substring(0, maxlen).lastIndexOf(" ");
			if (idx <= 0) idx = maxlen;
			al.add(line.substring(0, idx));
			line = line.substring(idx + 1);
		}
		al.add(line);

		return al;
	}

	public static String trimRight(String text) {
		while (text.endsWith(" ")) {
			text = text.substring(0, text.length() - 1);
		}
		return text;
	}

	public static String getPrefix(String text, char[] prefixChars) {
		int len = text.length();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			if (contains(prefixChars, c)) {
				sb.append(c);
			} else {
				break;
			}
		}
		return sb.toString();
	}

	public static boolean contains(char[] list, char element) {
		for (int i = 0; i < list.length; i++) {
			if (list[i] == element) return true;
		}
		return false;
	}

	public static String parseMailQuotationPrefix(String line) {
		StringBuilder sb = new StringBuilder();
		line = line.trim();
		while (line.startsWith(">") || line.startsWith(" ")) {
			sb.append(line.charAt(0));
			line = line.substring(1);
		}
		return sb.toString();
	}

	public static String quote(String text, String prefix) {
		List<String> lines = toStringList(text);
		StringBuilder sb = new StringBuilder();
		int size = lines.size();
		for (int i = 0; i < size; i++) {
			sb.append(prefix).append(lines.get(i)).append("\n");
		}
		return sb.toString();
	}

	public static String quoteMail(String text, String prefix, int maxlen) {
		if (text == null) return null;
		List<String> lines = toStringList(text);
		StringBuilder sb = new StringBuilder();
		int size = lines.size();
		for (int i = 0; i < size; i++) {
			String line = lines.get(i);
			int len = line.length();
			if (len > maxlen - 1) {
				String lineprefix = parseMailQuotationPrefix(line);
				int lplen = lineprefix.length();
				ArrayList<String> al = splitWordLineToList(line.substring(lplen), maxlen - lplen);
				for (int j = 0; j < al.size(); j++) {
					String l = al.get(j);
					l = trimRight(l);
					sb.append(prefix).append(lineprefix).append(l).append("\n");
				}
			} else {
				sb.append(prefix).append(line).append("\n");
			}
		}
		return sb.toString();
	}

	public static List<String> toStringList(String text) {
		BufferedReader in = new BufferedReader(new StringReader(text));
		List<String> lines = new ArrayList<String>();
		String line;
		try {
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	public static String escapeEscapeSequences(String text) {
		for (String[] esc : ESCAPE_SEQUENCES) {
			text = text.replace(esc[0], esc[1]);
		}
		return text;
	}

	public static class Char {

		public static final char BACKSPACE = 8;

		public static final char CR = 13;

		public static final char LF = 10;
	}

}
