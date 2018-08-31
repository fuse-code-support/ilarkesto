package ilarkesto.core.html;

import ilarkesto.core.base.Str;

import java.util.Collection;

public class Html {

	public static String convertHtmlToText(String html) {
		if (html == null) return null;

		html = getHtmlBody(html);

		StringBuilder sb = new StringBuilder();
		StringBuilder tag = null;

		boolean inside = false;
		int len = html.length();
		String href = null;
		char cPrev = (char) -1;
		for (int i = 0; i < len; i++) {
			char c = html.charAt(i);
			if (inside) {
				// inside html tag declaration
				if (c == '>') {
					inside = false;
					String t = tag.toString().toLowerCase();
					if (isTag(t, "br") || isTag(t, "ul") || isTag(t, "/ul") || isTag(t, "/dl") || isTag(t, "div")) {
						sb.append("\n");
					} else if (isTag(t, "dd") || isTag(t, "/td")) {
						sb.append("    ");
					} else if (isTag(t, "p") || isTag(t, "h1") || isTag(t, "h2") || isTag(t, "h3") || isTag(t, "h4")
							|| isTag(t, "h5") || isTag(t, "h6") || isTag(t, "dt") || isTag(t, "tr")) {
						sb.append("\n\n");
					} else if (isTag(t, "li")) {
						sb.append("\n- ");
					} else if (isTag(t, "hr")) {
						sb.append("\n--------------------\n");
					} else if (isTag(t, "/a")) {
						if (href != null) {
							int hrefLen = href.length();
							if (i > hrefLen * 2 && html.substring(i - 3 - hrefLen, i - 3).equalsIgnoreCase(href)) {
								// skip
							} else {
								sb.append(" [ ").append(href).append(" ]");
							}
							href = null;
						}
					} else if (isTag(t, "a")) {
						int idx = t.indexOf("href=\"");
						if (idx >= 0) {
							idx += 6;
							int endidx = t.indexOf("\"", idx);
							if (endidx > idx) {
								href = t.substring(idx, endidx);
							}
						} else {
							idx = t.indexOf("href='");
							if (idx >= 0) {
								idx += 6;
								int endidx = t.indexOf("'", idx);
								if (endidx > idx) {
									href = t.substring(idx, endidx);
								}
							}
						}
					}
					tag = null;
				} else {
					tag.append(c);
				}
				cPrev = c;
				continue;
			} else {
				// outside html tag
				if (c == '<') {
					inside = true;
					tag = new StringBuilder();
					cPrev = c;
					continue;
				}
				if (c == '\n' || c == '\r' || (isWhitespace(c) && isWhitespace(cPrev))) {
					// skip
				} else {
					sb.append(c);
				}
			}
			cPrev = c;
		}

		html = sb.toString();

		html = html.replace("&nbsp;", " ");
		html = html.replace("&#160;", " ");

		html = html.replace("&auml;", String.valueOf(Str.ae));
		html = html.replace("&#228;", String.valueOf(Str.ae));
		html = html.replace("&Auml;", String.valueOf(Str.AE));
		html = html.replace("&#196;", String.valueOf(Str.AE));

		html = html.replace("&uuml;", String.valueOf(Str.ue));
		html = html.replace("&#252;", String.valueOf(Str.ue));
		html = html.replace("&Uuml;", String.valueOf(Str.UE));
		html = html.replace("&#220;", String.valueOf(Str.UE));

		html = html.replace("&ouml;", String.valueOf(Str.oe));
		html = html.replace("&#246;", String.valueOf(Str.oe));
		html = html.replace("&Ouml;", String.valueOf(Str.OE));
		html = html.replace("&#214;", String.valueOf(Str.OE));

		html = html.replace("&szlig;", String.valueOf(Str.sz));
		html = html.replace("&#223;", String.valueOf(Str.sz));
		html = html.replace("&euro;", String.valueOf(Str.EUR));
		html = html.replace("&#8211;", "-");
		html = html.replace("&amp;", "&");
		html = html.replace("&quot;", "\"");
		html = html.replace("&lt;", "<");
		html = html.replace("&gt;", ">");
		html = html.replace("&bdquo;", "„");
		html = html.replace("&ldquo;", "“");
		html = html.replace("&ndash;", "–");
		html = html.replace("&#173;", "");
		html = html.replace("<br>", "\n");

		html = html.replace(" \n", "\n");
		html = html.replace("  \n", "\n");
		html = html.replace("   \n", "\n");
		html = html.replace("\n\n\n\n\n", "\n\n");
		html = html.replace("\n\n\n\n", "\n\n");
		html = html.replace("\n\n\n", "\n\n");

		return html.trim();
	}

	public static boolean isWhitespace(char c) {
		// return Character.isWhitespace(c);
		if (c == ' ' || c == '\n' || c == '\r' || c == '\t') return true;
		return false;
	}

	public static String getHtmlBody(String html) {
		if (html == null) return null;

		// TODO convert encoding if not UTF-8

		int idx = html.indexOf("<body");
		if (idx < 0) idx = html.indexOf("<BODY");
		if (idx < 0) return html;

		int startIdx = html.indexOf('>', idx);
		if (startIdx < 0) return html;
		startIdx++;

		int endIdx = html.indexOf("</body>", startIdx);
		if (endIdx < 0) endIdx = html.indexOf("</BODY>", startIdx);
		if (endIdx < 0) return html.substring(startIdx);

		return html.substring(startIdx, endIdx);
	}

	public static String getFirstLineFromHtml(String text, int cutAfterLength, String appendAfterCut) {
		if (text == null) return "<empty>";
		if (text.startsWith("<html")) {
			text = cutHtmlAndHeaderAndBody(text);
			text = removeHtmlTags(text).trim();
		} else {
			text = Str.getFirstLine(text);
		}
		text = Str.getFirstLine(text, cutAfterLength, appendAfterCut);
		return text;
	}

	public static String getLineFromHtml(String text, int line, int cutAfterLength, String appendAfterCut) {
		if (text == null) return "<empty>";
		if (text.startsWith("<html")) {
			text = cutHtmlAndHeaderAndBody(text);
			text = removeHtmlTags(text).trim();
		}
		text = Str.getLine(text, line, cutAfterLength, appendAfterCut);
		return text;
	}

	public static String cutHtmlAndHeaderAndBody(String s) {
		if (s == null) return null;
		if (s.startsWith("<html")) {
			int idx = s.indexOf('>');
			s = s.substring(idx + 1).trim();
		}
		if (s.endsWith("</html>")) {
			s = s.substring(0, s.length() - 7).trim();
		}
		if (s.startsWith("<head>")) {
			int endIdx = s.indexOf("</head>");
			s = s.substring(endIdx + 7).trim();
		}
		if (s.startsWith("<body")) {
			int from = s.indexOf('>');
			int to = s.indexOf("</body>");
			s = s.substring(from + 1, to).trim();
		}
		return s;
	}

	private static boolean isTag(String tag, String name) {
		return tag.equals(name) || tag.startsWith(name + " ");
	}

	public static String removeHtmlTags(String s) {
		if (s == null) return null;
		StringBuilder sb = new StringBuilder();

		boolean inside = false;
		int len = s.length();
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (inside) {
				// inside html tag
				if (c == '>') inside = false;
				continue;
			} else {
				// outside html tag
				if (c == '<') {
					inside = true;
					continue;
				}
				sb.append(c);
			}
		}

		s = sb.toString();
		s = s.replace("&nbsp;", " ");
		s = s.replace("&auml;", String.valueOf(Str.ae));
		s = s.replace("&uuml;", String.valueOf(Str.ue));
		s = s.replace("&ouml;", String.valueOf(Str.oe));
		s = s.replace("&Auml;", String.valueOf(Str.AE));
		s = s.replace("&Uuml;", String.valueOf(Str.UE));
		s = s.replace("&Ouml;", String.valueOf(Str.OE));
		s = s.replace("&szlig;", String.valueOf(Str.sz));
		s = s.replace("&euro;", String.valueOf(Str.EUR));
		s = s.replace("&amp;", "&");
		s = s.replace("&quot;", "\"");
		s = s.replace("&lt;", "<");
		s = s.replace("&gt;", ">");
		s = s.replace("<br>", "\n");
		return s;
	}

	public static String concatToHtml(Collection<? extends ToHtmlSupport> items, String separator) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (ToHtmlSupport entity : items) {
			if (first) {
				first = false;
			} else {
				sb.append(separator);
			}
			sb.append(entity.toHtml());
		}
		return sb.toString();
	}

	// public static String removeSomeHtmlTags(String s, TagPredicate tagsToRemove) {
	// if (s == null) return null;
	// StringBuilder sb = new StringBuilder();
	// StringBuilder currentTagPartial = null;
	// boolean currentTagFinished = true;
	//
	// boolean inside = false;
	// int len = s.length();
	// for (int i = 0; i < len; i++) {
	// char c = s.charAt(i);
	// if (inside) {
	// // inside html tag
	// if (c == '>') {
	// inside = false;
	// currentTagPartial = null;
	// currentTagFinished = true;
	// if (tagsToRemove.accept(currentTagPartial.toString())) {
	// sb.append("<")
	// }
	// } else if (c == ' ') {
	// currentTagFinished = true;
	// } else {
	// if (!currentTagFinished) currentTagPartial.append(c);
	// }
	// continue;
	// } else {
	// // outside html tag
	// if (c == '<') {
	// inside = true;
	// currentTagPartial = new StringBuilder();
	// currentTagFinished = false;
	// continue;
	// }
	// sb.append(c);
	// }
	// }
	//
	// s = sb.toString();
	// return s;
	// }
	//
	// public static interface TagPredicate {
	//
	// boolean accept(String tag);
	// }

}
