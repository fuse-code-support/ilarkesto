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
package ilarkesto.text;

import ilarkesto.core.base.Str;

import java.util.Random;

public class TextGenerator {

	private static final Random random = new Random(System.currentTimeMillis());

	private static int randomInt(int min, int max) {
		return random.nextInt(max - min + 1) + min;
	}

	private static char randomChar(String charSet) {
		int index = randomInt(0, charSet.length() - 1);
		return charSet.charAt(index);
	}

	public static String password() {
		return password(randomInt(12, 24));
	}

	public static String password(int length) {
		if (length <= 10)
			return word("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!$%&=-_/()[]{}", length);

		StringBuilder sb = new StringBuilder();
		length -= 3;

		while (length > 6) {
			String word = word(2, 5, true) + " ";
			length -= word.length();
			sb.append(word);
		}
		sb.append(word(length, length, true));

		sb.append(' ').append(word("1234567890", "!$%&=-_/()[]{}", 2));

		return sb.toString();
	}

	public static String paragraphs(int count) {
		return paragraphs(count, null, null, "\n\n");
	}

	public static String paragraphs(int count, String prefix, String suffix, String separator) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			if (separator != null && i > 0) sb.append(separator);
			if (prefix != null) sb.append(prefix);
			sb.append(paragraph());
			if (suffix != null) sb.append(suffix);
		}
		return sb.toString();
	}

	public static String paragraph() {
		return paragraph(2, 10, 4, 12, 2, 12);
	}

	public static String paragraph(int minSentences, int maxSentences, int minWords, int maxWords, int minWordLenght,
			int maxWordLenght) {
		int sentences = randomInt(minSentences, maxSentences);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sentences; i++) {
			String sentence = sentence(minWords, maxWords, minWordLenght, maxWordLenght);
			if (i != 0) sb.append(" ");
			sb.append(sentence).append(".");
		}
		return sb.toString();
	}

	public static String sentence() {
		return sentence(4, 12);
	}

	public static String sentence(int minWords, int maxWords) {
		return sentence(minWords, maxWords, 2, 12);
	}

	public static String sentence(int minWords, int maxWords, int minWordLenght, int maxWordLenght) {
		int words = randomInt(minWords, maxWords);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < words; i++) {
			boolean uppercase = i == 0 || randomInt(0, 9) == 0;
			String word = word(minWordLenght, maxWordLenght, uppercase);
			if (i != 0) sb.append(" ");
			sb.append(word);
		}
		return sb.toString();
	}

	public static String word(int minLength, int maxLength, boolean uppercase) {
		String vovels = "aeiouy";
		String consonants = "bcdfghjklmnpqrstvwxz";
		int length = randomInt(minLength, maxLength);
		String word = word(vovels, consonants, length);
		return uppercase ? Str.uppercaseFirstLetter(word) : word;
	}

	public static String number(int lenght) {
		return word("123456789", lenght);
	}

	public static String word(String availableChars, int minLength, int maxLength) {
		int length = randomInt(minLength, maxLength);
		return word(availableChars, length);
	}

	public static String word(String charSet1, String charSet2, int length) {
		StringBuilder password = new StringBuilder();
		String charSet = charSet1;
		for (int i = 0; i < length; i++) {
			if (randomInt(0, 8) != 0) {
				if (charSet.equals(charSet1)) {
					charSet = charSet2;
				} else {
					charSet = charSet1;
				}
			}
			password.append(randomChar(charSet));
		}
		return password.toString();
	}

	public static String word(String availableChars, int length) {
		StringBuilder password = new StringBuilder();
		for (int i = 0; i < length; i++) {
			password.append(randomChar(availableChars));
		}
		return password.toString();
	}

}
