/**
 * 
 */
package org.glite.slcs.shibclient;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.cookie.RFC2109Spec;

/**
 * Debugging class for RFC 2109 cookie spec handler.
 * 
 * @author tschopp
 */
public class DebugCookiePolicy extends RFC2109Spec {

    static public String ID = "debug-rfc2109";

    public boolean domainMatch(String host, String domain) {
        boolean match = super.domainMatch(host, domain);
        System.out.println("XXX:domainMatch: host=" + host + " domain="
                + domain + " ? " + match);
        return match;
    }

    public boolean match(String host, int port, String path, boolean secure,
            Cookie cookie) {
        boolean match = super.match(host, port, path, secure, cookie);
        System.out.println("XXX:match: host=" + host + " port=" + port
                + " path=" + path + " secure=" + secure + " cookie=" + cookie
                + " ? " + match);
        return match;
    }

}
