/**
 * 
 */
package org.glite.slcs.shibclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpecBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.glite.slcs.httpclient.ssl.ExtendedProtocolSocketFactory;

/**
 * @author tschopp
 */
public class AuthnUsernamePassword {

    public static void main(String[] args) throws Exception {
        String truststore = "truststore.slcs.jks";
        ProtocolSocketFactory protocolSocketFactory = new ExtendedProtocolSocketFactory(
                truststore);
        Protocol https = new Protocol("https", protocolSocketFactory, 443);
        Protocol.registerProtocol("https", https);

        CookiePolicy.registerCookieSpec(DebugCookiePolicy.ID, DebugCookiePolicy.class);
        HttpClient httpClient = new HttpClient();
        String cookiePolicy = DebugCookiePolicy.ID; //CookiePolicy.DEFAULT;
        System.out.println("set CookiePolicy: " + cookiePolicy);
        httpClient.getParams().setCookiePolicy(cookiePolicy);
        String idpSsoUrl = "https://aai-logon.switch.ch/idp/profile/Shibboleth/SSO";
        String ssoUrl = idpSsoUrl
                + "?shire=https://slcs.switch.ch/Shibboleth.sso/SAML/POST&target=cookie&providerId=https://slcs.switch.ch/shibboleth";
/*
        GetMethod getRedirectMethod = new GetMethod(ssoUrl);
        getRedirectMethod.setFollowRedirects(false);
        httpClient.executeMethod(getRedirectMethod);
        dumpCookies(httpClient);

        Header locationHeader = getRedirectMethod.getResponseHeader("Location");
        if (locationHeader == null) {
            System.out.println("ERROR: not Location HTTP header");
            getRedirectMethod.releaseConnection();
            System.exit(1);
        }
        String redirectLocation = locationHeader.getValue();
        System.out.println("Location: " + redirectLocation);
        GetMethod getLoginPageMethod = new GetMethod(redirectLocation);
        
        URI getLoginPageMethodURI= getLoginPageMethod.getURI();
        String host = getLoginPageMethodURI.getHost();
        String path = getLoginPageMethodURI.getPath();
        Cookie cookies[] = getMatchingCookies(httpClient,host, path);
        for (int j = 0; j < cookies.length; j++) {
            System.out.println("setting Cookie: " + cookies[j]);
            getLoginPageMethod.addRequestHeader("Cookie", cookies[j].toString());
        }
*/
        
        GetMethod getLoginPageMethod = new GetMethod(ssoUrl);


        httpClient.executeMethod(getLoginPageMethod);
        dumpCookies(httpClient);

        // read response
        InputStream is = getLoginPageMethod.getResponseBodyAsStream();
        dumpResponse(is);
        getLoginPageMethod.releaseConnection();
        
    }

    public static StringBuffer getContent(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader in = new BufferedReader(isr);
        StringBuffer html = new StringBuffer();
        String line = null;
        while ((line = in.readLine()) != null) {
            html.append(line).append("\n");
        }
        return html;

    }

    public static void dumpResponse(InputStream is) throws IOException {
        System.out.println("---RESPONSE BEGIN---");
        System.out.print(getContent(is));
        System.out.println("---RESPONSE END---");

    }

    static public void dumpCookies(HttpClient httpClient) {
        Cookie[] cookies = httpClient.getState().getCookies();
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        sb.append("---[CookiePolicy=").append(
                                              httpClient.getParams().getCookiePolicy()).append(
                                                                                               "]---\n");
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            String path = cookie.getPath();
            String domain = cookie.getDomain();
            boolean secure = cookie.getSecure();
            int version = cookie.getVersion();
            // sb.append(name).append('=').append(value).append("\n");
            sb.append(i).append(": ").append(cookie).append("\n");
            sb.append("   domain:").append(domain).append("\n");
            sb.append("   path:").append(path).append("\n");
            sb.append("   secure:").append(secure).append("\n");
            sb.append("   version:").append(version).append("\n");
            sb.append("\n");
        }
        sb.append("---[End]---");
        System.out.println(sb.toString());

    }

    static public Cookie[] getMatchingCookies(HttpClient httpClient, String host,
            String path) {
        System.out.println("search all Cookies matching for host:" + host
                + " path:" + path);

        List<Cookie> matchingCookies = new ArrayList<Cookie>();
        Cookie[] cookies = httpClient.getState().getCookies();
        CookieSpecBase cookieSpecBase = new CookieSpecBase();
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookieSpecBase.match(host, 443, path, true, cookie)) {
                System.out.println("Cookie " + cookie + " matched");
                matchingCookies.add(cookie);
            }
        }
        return (Cookie[]) matchingCookies.toArray(new Cookie[matchingCookies.size()]);
    }

}
