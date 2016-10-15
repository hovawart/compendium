package com.nobodyelses.data.router;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class IceScheduleServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Default the date to today for the Pacific timezone, Los Angeles is the closest.
		String date = req.getParameter("date");
		if (date == null) {
			DateTime utc = new DateTime(DateTimeZone.UTC);
			DateTimeZone tz = DateTimeZone.forID("America/Los_Angeles");
			DateTime losAngelesDateTime = utc.toDateTime(tz);
			System.out.println(losAngelesDateTime);
			DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MM/dd/yyyy");
			date = dtfOut.print(losAngelesDateTime);

		}
		// End

		URLFetchService service = URLFetchServiceFactory.getURLFetchService();
		URL url = new URL("http://www.lasvegasice.com/DesktopModules/RMSCustomCalendar/API/Service/Events5");
		HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST );

		// Add request header that match what the browser sends so LVIC sends back the schedule every time.
		request.addHeader(new HTTPHeader("Cache-Control", "no-cache"));
		request.addHeader(new HTTPHeader("Host", "www.lasvegasice.com"));
		request.addHeader(new HTTPHeader("ModuleId", "543"));
		request.addHeader(new HTTPHeader("Origin", "http://www.lasvegasice.com"));
		request.addHeader(new HTTPHeader("Pragma", "no-cache"));
		request.addHeader(new HTTPHeader("Referer", "http://www.lasvegasice.com/ICESCHEDULE.aspx"));
		request.addHeader(new HTTPHeader("TabId", "119"));
		request.addHeader(new HTTPHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36"));
		// End

		// Encode the date to match what the browser sends to LVIC
		String encodedDate = URLEncoder.encode(date, "UTF-8");
		String payload = MessageFormat.format("StartDate={0}&ShowExpired=true&Customers=0&Categories=&DaysOut=14&IsCustomerFilter=true", encodedDate);
		// End

		request.setPayload(payload.getBytes());
		HTTPResponse response = service.fetch(request);
		byte[] content = response.getContent();
		String string = new String(content);
		System.out.println(string);
		resp.setContentLength(content.length);
		resp.setContentType("application/json; charset=UTF-8");
		resp.getOutputStream().write(content);
	}
}
