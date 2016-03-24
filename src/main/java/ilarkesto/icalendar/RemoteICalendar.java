package ilarkesto.icalendar;

import ilarkesto.core.logging.Log;
import ilarkesto.core.time.Tm;
import ilarkesto.net.httpclient.HttpSession;

public class RemoteICalendar extends ICalendar {

	public static void main(String[] args) {
		Log.setDebugEnabled(true);
		String url = "";
		new RemoteICalendar(url).update();
	}

	private static final Log log = Log.get(RemoteICalendar.class);

	private HttpSession http = new HttpSession();

	private String url;
	private long lastUpdateTime;

	public RemoteICalendar(String url) {
		super();
		this.url = url;
	}

	public RemoteICalendar update() {
		clear();
		log.info("Updating remote VCal:", url);
		String s = http.downloadText(url);
		parse(s);
		lastUpdateTime = Tm.getCurrentTimeMillis();
		return this;
	}

	public RemoteICalendar updateIfOlderThenMillis(long millis) {
		if (Tm.getCurrentTimeMillis() - lastUpdateTime > millis) update();
		return this;
	}

}
