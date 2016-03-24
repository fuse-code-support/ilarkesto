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
package ilarkesto.integration.kinode;

import ilarkesto.core.time.Date;
import ilarkesto.core.time.Time;
import ilarkesto.integration.kinode.KinoDe.Movie;
import ilarkesto.testng.ATest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

public class KinoDeTest extends ATest {

	@Test
	public void loadMovie() {
		Movie movie = KinoDe.loadMovie("star-wars-das-erwachen-der-macht-2015", observer);
		assertEquals(movie.title, "Star Wars: Das Erwachen der Macht");
		assertEquals(movie.posterUrl,
			"http://www.kino.de/wp-content/uploads/2015/12/star-wars-das-erwachen-der-macht-2015-filmplakat-rcm236x336u.jpg");
	}

	@Test
	public void loadShowsRinteln() {
		Consumer consumer = new Consumer();
		KinoDe.loadShows(KinoDe.CINEMA_ID_RINTELN, consumer, observer);
		assertNotEmpty(consumer.movieIds);
		assertTrue(consumer.dates.size() > 1);
	}

	@Test
	public void loadShowsBueckeburg() {
		Consumer consumer = new Consumer();
		KinoDe.loadShows(KinoDe.CINEMA_ID_BUECKEBURG, consumer, observer);
		assertNotEmpty(consumer.movieIds);
	}

	class Consumer implements MovieShowConsumer {

		private Set<String> movieIds = new HashSet<String>();
		private Set<Date> dates = new HashSet<Date>();

		@Override
		public void onMovieShow(String cinemaId, Date date, List<Time> times, String movieId, String movieTitle,
				String movieDescription, String movieCoverUrl) {
			movieIds.add(movieId);
			dates.add(date);
		}

	}
}
