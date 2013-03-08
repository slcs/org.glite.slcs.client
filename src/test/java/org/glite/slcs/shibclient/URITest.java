/**
 * 
 */
package org.glite.slcs.shibclient;

import junit.framework.TestCase;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

/**
 * @author tschopp
 *
 */
public class URITest extends TestCase {

	protected void setUp() throws Exception {
		System.out.println("---[START:" + getName() + "]---");
		super.setUp();

	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		System.out.println("---[DONE:" + getName() + "]---");
	}

    public void testURI() throws URIException {
        URI uri= new URI("https://localhost/path/test;jsessionid=ABCDEF?param1=value1",false);
        System.out.println("uri: " + uri);
        System.out.println("has auhtority: " + uri.hasAuthority());
        System.out.println("scheme: " + uri.getScheme());
        String host= uri.getHost();
        System.out.println("host: " + host);
        assertEquals("localhost", host);
        System.out.println("port: " + uri.getPort());
        String path= uri.getPath();
        System.out.println("path: " + path);
        String pathQuery= uri.getPathQuery();
        System.out.println("pathQuery: " + pathQuery);
        String abovePath= uri.getAboveHierPath();
        System.out.println("abovePath: " + abovePath);
        System.out.println("currentPath: " + uri.getCurrentHierPath());
        System.out.println("name: " + uri.getName());
        String query= uri.getQuery();
        System.out.println("query: " + query);
    }
    
    public void testPath() throws URIException {
        URI base= new URI("https://localhost/sso/cas/login1;blabla=titi?name=hello",false);
        System.out.println("base: " + base);
        URI action= new URI("login;jsessionid=ABCDEF?param=value",false);
        System.out.println("action: " + action);
        System.out.println("has authority: " + action.hasAuthority());
        System.out.println("is absolute path: " + action.isAbsPath());
        System.out.println("is relative path: " + action.isRelPath());
        System.out.println("path: " + action.getPath());
        URI full= new URI(base,action.getPathQuery(),false);
        System.out.println("full action: " + full);
    }
    
    public void testAbsoluteURI() throws URIException {
        URI abs0= new URI("http://localhost/toto?titi=tata",false);
        System.out.println(abs0 + " is absolute URI? " + abs0.isAbsoluteURI());
        System.out.println(abs0 + " is relative URI? " + abs0.isRelativeURI());
        URI abs1= new URI("/toto?titi=tata",false);
        System.out.println(abs1 + " is absolute URI? " + abs1.isAbsoluteURI());
        System.out.println(abs1 + " is relative URI? " + abs1.isRelativeURI());
        URI rel= new URI("toto?titi=tata",false);
        System.out.println(rel + " is absolute URI? " + rel.isAbsoluteURI());
        System.out.println(rel + " is relative URI? " + rel.isRelativeURI());
    }
    
    public void testNull() {
        try {
            new URI(null,false);
            fail("new URI(null,false) must fail");
        } catch (URIException e) {
            // must failed
        }
    }

}
