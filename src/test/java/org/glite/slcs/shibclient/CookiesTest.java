/*
 * $Id: CookiesTest.java,v 1.1 2007/10/01 10:46:11 vtschopp Exp $
 */
package org.glite.slcs.shibclient;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.cookie.CookieSpecBase;
import org.apache.commons.httpclient.cookie.MalformedCookieException;

import junit.framework.TestCase;

/**
 * Test case for the commons HttpClient cookies.
 */
public class CookiesTest extends TestCase {

	CookieSpecBase cookieSpecBase = null;

	protected void setUp() throws Exception {
		System.out.println("---[START:" + getName() + "]---");
		super.setUp();
		cookieSpecBase = new CookieSpecBase();

	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		System.out.println("---[DONE:" + getName() + "]---");
	}

	public void testDomainMatch() {
		String host = "auth.unige.ch";
		String cookieDomain = "auth.unige.ch";
		boolean domainMatched = cookieSpecBase.domainMatch(host, cookieDomain);
		assertTrue("host doesn't match cookie domain", domainMatched);
	}

	public void testPathMatch() {
		String path = "/cas/login";
		String cookiePath = "/cas";
		boolean pathMatched = cookieSpecBase.pathMatch(path, cookiePath);
		assertTrue("path doesn't match cookie path", pathMatched);

	}

	public void testMatch() {
		String cookieName = "JSESSIONID";
		String cookieValue = "74238923754987349";
		String cookieDomain = "auth.unige.ch";
		String cookiePath = "/cas";
		Cookie cookie = new Cookie(cookieDomain, cookieName, cookieValue);
		cookie.setPath(cookiePath);
		cookie.setSecure(true);

		String host = "auth.unige.ch";
		String path = "/cas/login";
		int port = 443; // ignored
		boolean secure = true;
		boolean matched = cookieSpecBase
				.match(host, port, path, secure, cookie);
		assertTrue("cookie doesn't match", matched);
	}

	public void testParse() {
		String header = "JSESSIONID=2E67B1A6DFB7136A1BBA485220DBEC12; Path=/cas; Secure";
		String host = "auth.unige.ch";
		String path = "/cas/login";
		int port = 3456; // ignored
		boolean secure = true;
		try {
			Cookie cookies[] = cookieSpecBase.parse(host, port, path, secure,
					header);
			for (Cookie cookie : cookies) {
				System.out.println(cookie.getName() + "=" + cookie.getValue()
						+ " domain:" + cookie.getDomain() + " path:"
						+ cookie.getPath() + " secure:" + cookie.getSecure());
				System.out.println(cookieSpecBase.formatCookieHeader(cookie));
			}
		} catch (MalformedCookieException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
