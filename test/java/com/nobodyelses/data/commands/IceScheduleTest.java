package com.nobodyelses.data.commands;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;

import junit.framework.TestCase; 

public class IceScheduleTest extends TestCase {
	private final LocalServiceTestHelper helper =
			new LocalServiceTestHelper(new LocalURLFetchServiceTestConfig());
	
	@Override
	@Before
	public void setUp() throws Exception {
		helper.setUp();
	}

	@Override
	@After
	public void tearDown() {
		helper.tearDown();
	}

	@Test
	public void test() throws Exception {
		URLFetchService service = URLFetchServiceFactory.getURLFetchService();
		URL url = new URL("http://www.lasvegasice.com/DesktopModules/RMSCustomCalendar/API/Service/Events5");
		HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST );
		request.setPayload("StartDate=07%2F24%2F2016&ShowExpired=true&Customers=0&Categories=&DaysOut=14&IsCustomerFilter=true".getBytes()); 
		HTTPResponse response = service.fetch(request);
		byte[] content = response.getContent();
		String string = new String(content);
		System.out.println(string);
	}

}
