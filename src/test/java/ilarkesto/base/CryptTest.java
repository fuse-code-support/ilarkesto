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
package ilarkesto.base;

import ilarkesto.base.Crypt.DecryptionFailedException;
import ilarkesto.testng.ATest;
import ilarkesto.text.TextGenerator;

import java.io.File;

import org.testng.annotations.Test;

public class CryptTest extends ATest {

	@Test
	public void file() throws DecryptionFailedException {
		String text = TextGenerator.paragraphs(10);
		String password = TextGenerator.password();
		Crypt crypt = Crypt.createAesInstance();
		File file = getTestOutputFile("file.txt");
		byte[] key = crypt.createKeyFromPassword(password);
		crypt.encryptToFile(text, key, file);
		String decrypted = crypt.decryptFileToString(file, key);
		assertEquals(decrypted, text);
	}

	@Test
	public void text() throws DecryptionFailedException {
		String text = TextGenerator.paragraphs(10);
		Crypt crypt = Crypt.createAesInstance();
		byte[] key = crypt.generateKey();
		byte[] encrypted = crypt.encrypt(text, key);
		String decrypted = crypt.decryptToString(encrypted, key);
		assertEquals(decrypted, text);
	}
}
