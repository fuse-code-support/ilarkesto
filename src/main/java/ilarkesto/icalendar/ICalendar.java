package ilarkesto.icalendar;

import ilarkesto.core.base.MultilineBuilder;
import ilarkesto.core.base.Str;
import ilarkesto.core.base.Tuple;
import ilarkesto.core.base.Utl;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.Date;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.core.time.Time;
import ilarkesto.io.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class ICalendar {

	private static final Log log = Log.get(ICalendar.class);

	public static void main(String[] args) {
		Log.setDebugEnabled(true);
		ICalendar cal = new ICalendar();
		// cal.parse(new File("G:/home/wko/inbox/kinostarts.ics"));
		cal.parse(new File("G:/home/wko/inbox/jug.ics"));
		// cal.parse(new File("G:/home/wko/inbox/feiertage.ics"));
		// cal.parseUrl("file:///G:/home/wko/inbox/kinostarts.ics");
		for (Event event : cal.events) {
			log.info(event);
		}
		Log.flush();
	}

	private String prodid = "-//Witoslaw Koczewski//Ilarkesto";
	private String version = "2.0";
	private String calscale = "GREGORIAN";
	private String mehtod = "PUBLISH";
	private String xWrCalname;
	private String xWrCaldesc;
	private String xWrTimezone;

	private final Timezone timezone = new Timezone();
	private final Set<Event> events = new HashSet<Event>();

	public ICalendar clear() {
		events.clear();
		return this;
	}

	public void parse(String s) {
		BufferedReader in = new BufferedReader(new StringReader(s));
		parse(in);
		try {
			in.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public ICalendar parse(File file) {
		if (!file.exists()) return null;
		FileInputStream in;
		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}
		parse(in);
		IO.close(in);

		return this;
	}

	public void parse(InputStream is) {
		try {
			parse(new BufferedReader(new InputStreamReader(is, IO.UTF_8)));
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void parse(BufferedReader in) {
		List<String> lines = IO.readLines(in);
		List<String> ret = new LinkedList<String>();
		String suffix = null;
		for (int i = lines.size() - 1; i >= 0; i--) {
			String line = lines.get(i);
			if (line.startsWith(" ")) {
				if (suffix == null) {
					suffix = line.substring(1);
				} else {
					suffix = line.substring(1) + suffix;
				}
			} else {
				if (suffix != null) {
					line = line + suffix;
					suffix = null;
				}
				ret.add(0, line);
			}
		}
		parse(ret);
	}

	public void parse(List<String> lines) {
		for (String line : lines) {
			if (line.length() == 0) continue;
			int idx = line.indexOf(':');
			if (idx < 1) throw new RuntimeException("VCAL-Syntax error: " + line);
			String key = line.substring(0, idx);
			String value = idx == line.length() - 1 ? null : line.substring(idx + 1);
			value = parseString(value);
			parse(key, value);
		}
	}

	private Event event;
	private boolean inTimezone;
	private boolean inAlarm;
	private boolean inFreeBusy;

	private void parse(String key, String value) {

		if (key.equals("BEGIN") && value.equals("VCALENDAR")) return;
		if (key.equals("BEGIN") && value.equals("VEVENT")) {
			event = new Event();
			return;
		}
		if (key.equals("BEGIN") && value.equals("VTIMEZONE")) {
			inTimezone = true;
			return;
		}
		if (key.equals("BEGIN") && value.equals("VALARM")) {
			inAlarm = true;
			return;
		}
		if (key.equals("BEGIN") && value.equals("VFREEBUSY")) {
			inFreeBusy = true;
			return;
		}

		// VTIMEZONE
		if (inTimezone) {
			if (key.equals("END") && value.equals("VTIMEZONE")) {
				inTimezone = false;
				return;
			}
			return;
		}

		// VALARM
		if (inAlarm) {
			if (key.equals("END") && value.equals("VALARM")) {
				inAlarm = false;
				return;
			}
			return;
		}

		// VFREEBUSY
		if (inFreeBusy) {
			if (key.equals("END") && value.equals("VFREEBUSY")) {
				inFreeBusy = false;
				return;
			}
			return;
		}

		// VEVENT
		if (event != null) {
			if (key.equals("END") && value.equals("VEVENT")) {
				log.debug("Event parsed:", event);
				events.add(event);
				event = null;
				return;
			}
			if (key.startsWith("RECURRENCE-ID")) {
				// TODO recurrence
				return;
			}
			if (key.startsWith("EXDATE")) {
				// TODO exdate
				return;
			}
			if (key.startsWith("ATTENDEE")) {
				// TODO attendee
				return;
			}
			if (key.startsWith("ORGANIZER")) {
				event.organizer = value;
				return;
			}
			if (key.startsWith("ATTACH")) {
				// TODO attach
				return;
			}
			if (key.equals("SUMMARY")) {
				event.summary = value;
				return;
			}
			if (key.equals("DESCRIPTION")) {
				event.description = value;
				return;
			}
			if (key.equals("LOCATION")) {
				event.location = value;
				return;
			}
			if (key.equals("CLASS")) {
				event.classs = value;
				return;
			}
			if (key.equals("UID")) {
				event.uid = value;
				return;
			}
			if (key.equals("TRANSP")) {
				event.transp = value;
				return;
			}
			if (key.equals("STATUS")) {
				event.status = value;
				return;
			}
			if (key.equals("SEQUENCE")) {
				event.sequence = Integer.valueOf(value);
				return;
			}
			if (key.equals("CREATED")) {
				event.created = parseDateAndTime(value);
				return;
			}
			if (key.equals("LAST-MODIFIED")) {
				event.lastModified = parseDateAndTime(value);
				return;
			}
			if (key.equals("DTSTAMP")) {
				event.stamp = parseDateAndTime(value);
				return;
			}
			if (key.startsWith("DTSTART")) {
				Tuple<Date, Time> dt = parseDateOrDateAndTime(value);
				event.date = dt.getA();
				event.start = dt.getB();
				return;
			}
			if (key.startsWith("DTEND")) {
				Tuple<Date, Time> dt = parseDateOrDateAndTime(value);
				if (dt.getA().equals(event.date)) {
					event.end = dt.getB();
				}
				return;
			}
			if (key.equals("RRULE")) {
				// TODO RRULE
				return;
			}
			if (key.equals("CATEGORIES")) {
				// TODO CATEGORIES
				return;
			}

			if (key.equals("URL")) {
				event.url = value;
				return;
			}

			log.warn("Unsupported event field:", key, "->", value);
			return;
		}

		// VCALENDAR
		if (key.equals("END") && value.equals("VCALENDAR")) return;
		if (key.equals("PRODID")) {
			prodid = value;
			return;
		}
		if (key.equals("VERSION")) {
			version = value;
			return;
		}
		if (key.equals("CALSCALE")) {
			calscale = value;
			return;
		}
		if (key.equals("METHOD")) {
			mehtod = value;
			return;
		}
		if (key.equals("X-WR-CALNAME")) {
			xWrCalname = value;
			return;
		}
		if (key.equals("X-WR-TIMEZONE")) {
			xWrTimezone = value;
			return;
		}
		if (key.equals("X-WR-CALDESC")) {
			xWrCaldesc = value;
			return;
		}

		log.warn("Unsupported field:", key, "->", value);
	}

	private String parseString(String s) {
		if (s == null) return null;
		s = s.replace("\\n", "\n");
		s = s.replace("\\,", ",");
		return s;
	}

	private DateAndTime parseDateAndTime(String s) {
		try {
			if (s.endsWith("Z")) {
				return parseDateAndTime(s, FORMAT1_UTC, FORMAT2_UTC);
			} else {
				// TODO consider WR-TIMEZONE
				return parseDateAndTime(s, FORMAT1_DE, FORMAT2_DE);
			}
		} catch (ParseException ex) {
			throw new RuntimeException("Parsing date/time failed: " + s);
		}
	}

	public static DateAndTime parseDateAndTime(String s, DateFormat... formats) throws ParseException {
		ParseException ex = null;
		for (DateFormat format : formats) {
			try {
				return new DateAndTime(format.parse(s));
			} catch (ParseException e) {
				ex = e;
			}
		}
		throw ex;
	}

	private static final transient SimpleDateFormat FORMAT1_DE = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	private static final transient SimpleDateFormat FORMAT2_DE = new SimpleDateFormat("yyyyMMdd");
	private static final transient SimpleDateFormat FORMAT1_UTC = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	private static final transient SimpleDateFormat FORMAT2_UTC = new SimpleDateFormat("yyyyMMdd");

	static {
		FORMAT1_UTC.setTimeZone(TimeZone.getTimeZone("UTC"));
		FORMAT2_UTC.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private Tuple<Date, Time> parseDateOrDateAndTime(String s) {
		DateAndTime dt = parseDateAndTime(s);

		if (s.contains("T")) {
			return new Tuple<Date, Time>(dt.getDate(), dt.getTime());
		} else {
			return new Tuple<Date, Time>(dt.getDate(), null);
		}
	}

	public Set<Event> getEvents() {
		return events;
	}

	public List<Event> getEventsOnDate(Date date) {
		ArrayList<Event> ret = new ArrayList<Event>();
		for (Event event : events) {
			if (date.equals(event.getDate())) ret.add(event);
		}
		return ret;
	}

	public String getxWrCalname() {
		return xWrCalname;
	}

	@Override
	public String toString() {
		MultilineBuilder mb = new MultilineBuilder();
		mb.ln("BEGIN:VCALENDAR");

		writeString(mb, "PRODID", prodid);
		writeString(mb, "VERSION", version);
		writeString(mb, "CALSCALE", calscale);
		writeString(mb, "METHOD", mehtod);
		writeString(mb, "X-WR-CALNAME", xWrCalname);
		writeString(mb, "X-WR-TIMEZONE", xWrTimezone);
		writeString(mb, "X-WR-CALNAME", xWrCalname);
		writeString(mb, "X-WR-CALDESC", xWrCaldesc);

		// TODO timezone

		for (Event event : events) {
			event.write(mb);
		}

		mb.ln("END:VCALENDAR");
		return mb.toString();
	}

	private static final void writeString(MultilineBuilder mb, String name, String value) {
		if (value == null) return;
		// TODO multiline
		mb.ln(name + ":" + value);
	}

	private static final void writeDateAndTime(MultilineBuilder mb, String name, DateAndTime value) {
		if (value == null) return;
		// TODO multiline
		mb.ln(name + ":" + FORMAT1_UTC.format(value.toJavaDate()) + "Z");
	}

	public static class Event implements Comparable<Event> {

		private Date date;
		private Time start;
		private Time end;
		private DateAndTime stamp;
		private String uid;
		private String classs;
		private DateAndTime created;
		private DateAndTime lastModified;
		private String location;
		private Integer sequence;
		private String status;
		private String summary;
		private String description;
		private String transp;
		private String url;
		private String organizer;

		public String getOrganizer() {
			return organizer;
		}

		public void write(MultilineBuilder mb) {
			mb.ln("BEGIN:VEVENT");

			writeString(mb, "DTSTART", "");

			mb.ln("END:VEVENT");
		}

		public String getUrl() {
			return url;
		}

		public Date getDate() {
			return date;
		}

		public Time getStart() {
			return start;
		}

		public Time getEnd() {
			return end;
		}

		public DateAndTime getStamp() {
			return stamp;
		}

		public String getUid() {
			return uid;
		}

		public String getClasss() {
			return classs;
		}

		public DateAndTime getCreated() {
			return created;
		}

		public DateAndTime getLastModified() {
			return lastModified;
		}

		public String getLocation() {
			return location;
		}

		public Integer getSequence() {
			return sequence;
		}

		public String getStatus() {
			return status;
		}

		public boolean isStatus(String status) {
			return Utl.equals(this.status, status);
		}

		public boolean isStatusConfirmed() {
			return isStatus("CONFIRMED");
		}

		public String getSummary() {
			return summary;
		}

		public String getDescription() {
			return description;
		}

		public String getTransp() {
			return transp;
		}

		@Override
		public String toString() {
			return date + " " + start + "-" + end + ": " + summary;
		}

		public boolean isUrlSet() {
			return !Str.isBlank(url);
		}

		public String getSummaryAndLocation() {
			MultilineBuilder mb = new MultilineBuilder();
			mb.ln(getSummary());
			mb.ln(location);
			return mb.toString();
		}

		@Override
		public int compareTo(Event other) {
			int ret = Utl.compare(getDate(), other.getDate());
			if (ret != 0) return ret;

			ret = Utl.compare(getStart(), other.getStart());
			if (ret != 0) return ret;

			return Utl.compare(getSummaryAndLocation(), other.getSummaryAndLocation());
		}

	}

	public static class Timezone {

		private String id; // TZID
		private String location; // X-LIC-LOCATION

	}

	public static class RecurrenceRule {

		public static final String FREQ_MONTHLY = "MONTHLY";

		private String freq;
		private Integer count;
		private Integer bymonthday;

		public boolean isMonthly() {
			return FREQ_MONTHLY.equals(freq);
		}

	}

}
