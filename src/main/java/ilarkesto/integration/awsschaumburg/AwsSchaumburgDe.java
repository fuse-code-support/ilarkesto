package ilarkesto.integration.awsschaumburg;

import ilarkesto.core.base.MapBuilder;
import ilarkesto.core.base.OperationObserver;
import ilarkesto.core.base.Parser;
import ilarkesto.core.base.Parser.ParseException;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.Date;
import ilarkesto.core.time.Tm;
import ilarkesto.net.httpclient.HttpSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class AwsSchaumburgDe {

	private static Log log = Log.get(AwsSchaumburgDe.class);
	private static final String CHARSET = "ISO-8859-1";
	public static final String BASE_URL = "http://aws-shg.de/";
	private static final String LISTE_URL = BASE_URL + "abfuhr/Liste.php?ortid=17";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
	private static final Date minDate = Date.beforeDays(14);

	public static List<WastePickupArea> loadPickupAreas() throws ParseException {
		GregorianCalendar calendar = new GregorianCalendar(Locale.GERMANY);
		int year = calendar.get(Calendar.YEAR);
		String url = LISTE_URL + "&curJahr=" + year;
		log.debug(url);

		HttpSession session = new HttpSession().setCharset(CHARSET);
		session.postAndDownloadText("http://aws-shg.de/abfuhr/getListe.php?ortid=17",
			new MapBuilder().put("ortid", "17").put("curJahr", String.valueOf(Tm.getCurrentYear())).getMap());
		String data = session.downloadText("http://aws-shg.de/abfuhr/getListe.php");

		Parser parser = new Parser(data);
		List<WastePickupArea> ret = new ArrayList<WastePickupArea>();
		parser.gotoAfter("<SELECT name=\"strassenid\"");
		while (parser.gotoAfterIf("<OPTION VALUE=\"")) {
			String id = parser.getUntil("\"");
			parser.gotoAfter(">");
			String label = parser.getUntil("<");
			ret.add(new WastePickupArea(Parser.text(label), Integer.parseInt(id)));
		}
		if (ret.isEmpty()) throw new ParseException("Parsing pickup areas failed: " + data);
		return ret;
	}

	public static void update(OperationObserver observer, WastePickupSchedule schedule) throws ParseException {
		log.info("Updating schedule:", schedule);
		schedule.removePickupDatesBefore(minDate);
		Date today = Date.today();
		update(observer, schedule, schedule.getAreaAwsId(), today.getYear());
		if (today.getMonth() == 12) update(observer, schedule, schedule.getAreaAwsId(), today.getYear() + 1);
	}

	private static void update(OperationObserver observer, WastePickupSchedule schedule, int areaId, int year)
			throws ParseException {
		String url = getListeUrl(year, areaId);
		observer.onOperationInfoChanged(OperationObserver.DOWNLOADING, url);
		String data = new HttpSession().setCharset(CHARSET).downloadText(url);
		ScheduleParser parser = new ScheduleParser(data, schedule);
		parser.parsePickups();
	}

	public static String getListeUrl(int year, int areaId) {
		return LISTE_URL + "&curJahr=" + year + "&SID=" + areaId;
	}

	static class ScheduleParser extends Parser {

		private final WastePickupSchedule schedule;

		private ScheduleParser(String data, WastePickupSchedule schedule) {
			super(data);
			this.schedule = schedule;
		}

		public void parsePickups() throws ParseException {
			while (gotoAfterIf("<tr class=\"art")) {
				parsePickup();
			}
		}

		private void parsePickup() throws ParseException {
			gotoAfter("<td");
			gotoAfter(">");
			String dateS = getUntil("</td>");
			Date date;
			try {
				date = new Date(DATE_FORMAT.parse(dateS));
			} catch (java.text.ParseException ex) {
				throw new RuntimeException("Parsing date failed: " + dateS, ex);
			}
			if (date.isBefore(minDate)) return;
			gotoAfter("<td");
			gotoAfter(">");
			String weekday = getUntil("</td>");
			gotoAfter("<td");
			gotoAfter(">");
			String waste = getUntil("</td>");
			gotoAfter("</tr>");
			schedule.addPickupDate(waste, date);
			log.info("Pickup date added:", dateS, "->", waste);
		}
	}

}
