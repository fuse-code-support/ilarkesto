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
package ilarkesto.core.persistance;

import ilarkesto.core.base.Factory;
import ilarkesto.persistence.ThreadlocalTransactionManager;
import ilarkesto.testng.ATest;

import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

public class ThreadlocalTransactionManagerTest extends ATest {

	@BeforeClass
	public void init() {
		Persistence.initialize(new Factory<EntitiesBackend>() {

			@Override
			public EntitiesBackend newInstance() {
				return new InMemoryEntitiesBackend();
			}
		}, new ThreadlocalTransactionManager());
	}

	@Test
	@ExpectedExceptions(value = { WriteInReadOnlyTransactionException.class })
	public void readOnly() {
		new Flower("rose").persist();
	}

	@Test
	public void simple() {
		Persistence.runInTransaction("a", new Runnable() {

			@Override
			public void run() {
				new Flower("tulip").persist();
			}
		});
	}

	@Test
	public void nestedWithWriteInInner() {
		Persistence.runInTransaction("a", new Runnable() {

			@Override
			public void run() {

				Persistence.runInTransaction("a", new Runnable() {

					@Override
					public void run() {
						new Flower("tulip").persist();
					}
				});

			}
		});
	}

	@Test
	public void nestedWithReadAndWrites() {

		log.info("out", Flower.listAll());

		Persistence.runInTransaction("a", new Runnable() {

			@Override
			public void run() {
				log.info("in a", Flower.listAll());
				new Flower("sunflower").persist();
				Persistence.runInTransaction("a", new Runnable() {

					@Override
					public void run() {
						log.info("in b", Flower.listAll());
						new Flower("tulip").persist();
					}
				});
				log.info("back in a", Flower.listAll());
				new Flower("cactus").persist();
			}
		});

		log.info("back out", Flower.listAll());
	}

	static class Flower extends AEntity {

		public static Set<Flower> listAll() {
			return new AllByTypeQuery(Flower.class).list();
		}

		private String name;

		public Flower(String name) {
			super();
			this.name = name;
		}

		@Override
		protected String asString() {
			return name;
		}

	}

}
