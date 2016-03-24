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
package ilarkesto.io.cache;

import ilarkesto.base.Utl;
import ilarkesto.core.base.OperationObserver;
import ilarkesto.io.AFileStorage;
import ilarkesto.io.IO;
import ilarkesto.testng.ATest;

import org.testng.annotations.Test;

public class AFileStorageBackedCacheTest extends ATest {

	private String remoteValue;

	@Test
	public void testWorkflow() {
		remoteValue = "a";
		Cache cache = new Cache();
		assertNull(cache.getValue());

		cache.update(observer);
		assertEquals(cache.getValue(), "a");

		assertFalse(cache.isUpdateRequired());
		remoteValue = "b";
		cache.updateIfRequired(observer);
		assertEquals(cache.getValue(), "a");

		Utl.sleep(1000);
		assertTrue(cache.isUpdateRequired());
		cache.updateIfRequired(observer);
		assertEquals(cache.getValue(), "b");
	}

	class Cache extends AFileStorageBackedCache<String> {

		public Cache() {
			super(getTestFileStorage("cache"));
		}

		@Override
		protected void saveValueToCache(AFileStorage storage, String value) {
			IO.writeFile(storage.getFile("data.txt"), value, IO.UTF_8);
		}

		@Override
		protected String loadValueFromCache(AFileStorage storage) {
			return IO.readFile(storage.getFile("date.txt"), IO.UTF_8);
		}

		@Override
		protected String loadValueFromRemote(OperationObserver observer) {
			return remoteValue;
		}

		@Override
		protected boolean isUpdateRequired() {
			return isLastUpdatedLongerAgoThen(1000);
		}

	}

}
