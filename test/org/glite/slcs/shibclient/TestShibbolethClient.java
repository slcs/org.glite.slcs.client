/*
 * $Id: TestShibbolethClient.java,v 1.1 2006/10/24 09:24:19 vtschopp Exp $
 * 
 * Created on May 24, 2006 by tschopp
 *
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */
package org.glite.slcs.shibclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;

import org.glite.slcs.httpclient.ssl.ExtendedProtocolSocketFactory;
import org.glite.slcs.pki.Certificate;
import org.glite.slcs.pki.CertificateKeys;
import org.glite.slcs.pki.CertificateRequest;
import org.glite.slcs.shibclient.ShibbolethClient;
import org.glite.slcs.shibclient.ShibbolethCredentials;
import org.glite.slcs.shibclient.metadata.ShibbolethClientMetadata;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;

public class TestShibbolethClient {

    /**
     * @param args
     * @throws MalformedURLException
     */
    public static void main(String[] args) throws Exception {

        // create credentials
        // XXX WARNING PASSWORD IN SOURCE CODE
        // final String username= "test-tschopp";
        final String username= "vlry";
        final String password= "1lapin";
        final String idpProviderID= "urn:mace:switch.ch:aaitest:dukono.switch.ch";
        ShibbolethCredentials credentials= new ShibbolethCredentials(username,
                                                                     password,
                                                                     idpProviderID);
        // create metadata
        final String filename= "/Users/tschopp/projects/Java/SLCS-client/etc/slcs-client.xml";
        ShibbolethClientMetadata metadata= new ShibbolethClientMetadata(filename);

        // create httpclient
        String truststore= "/Users/tschopp/projects/Java/SLCS-client/etc/truststore.switchaai.jks";
        ExtendedProtocolSocketFactory protocolSocketFactory= new ExtendedProtocolSocketFactory(truststore);
        Protocol https= new Protocol("https", protocolSocketFactory, 443);
        Protocol.registerProtocol("https", https);
        HttpClient httpClient= new HttpClient();

        // create shib client
        ShibbolethClient client= new ShibbolethClient(httpClient,
                                                      metadata,
                                                      credentials);

        // SLCS login and certificate URLs
        String slcsHost= "https://macvt.switch.ch";
        String slcsLoginURL= slcsHost + "/SLCS/login";
        String slcsCertificateURL= slcsHost + "/SLCS/certificate";

        // shib login
        System.out.println("Authenticate with " + credentials);
        client.authenticate(slcsLoginURL);

        // SLCS login
        System.out.println("GET login: " + slcsLoginURL);
        GetMethod GETLogin= new GetMethod(slcsLoginURL);
        client.executeMethod(GETLogin);
        System.out.println(GETLogin.getStatusLine());
        // check status

        // read response
        InputStream is= GETLogin.getResponseBodyAsStream();
        StringBuffer loginResponse= getContent(is);
        GETLogin.releaseConnection();

        System.out.println(loginResponse);
        // parse response
        String dn= getDN(loginResponse);
//        System.out.println("DN=" + dn);
        String authToken= getAuthorizationToken(loginResponse);
//        System.out.println("AuthorizationToken=" + authToken);
        // TODO checks null

        // create key pair
        System.out.println("create and store keys...");
        char[] pass= password.toCharArray();
        CertificateKeys keys= new CertificateKeys(2048, pass);
        keys.storePEMPrivate("/var/tmp/" + username + "key.pem");
        // create csr
        System.out.println("create and store csr...");
        CertificateRequest csr= new CertificateRequest(keys, dn);
        csr.storePEM("/var/tmp/" + username + "cert_req.pem"); 
        // post csr
        PostMethod POSTCertificateRequestMethod= new PostMethod(slcsCertificateURL);
        POSTCertificateRequestMethod.addParameter("AuthorizationToken",
                                                  authToken);
        POSTCertificateRequestMethod.addParameter("CertificateSigningRequest",
                                                  csr.getPEMEncoded());
        System.out.println("POST: " + slcsCertificateURL);
        client.executeMethod(POSTCertificateRequestMethod);
        System.out.println(POSTCertificateRequestMethod.getStatusLine());
        // check status

        // parse and check response
        StringBuffer certificateResponse= getContent(POSTCertificateRequestMethod.getResponseBodyAsStream());
        POSTCertificateRequestMethod.releaseConnection();
        System.out.println(certificateResponse);

        // get certificate
        String pemCert= getCertificate(certificateResponse);
//        System.out.println("Certificate=" + pemCert);
        
        // parse and store certificate (with chain)
        System.out.println("parse and store cert...");
        StringReader reader= new StringReader(pemCert);
        Certificate cert= Certificate.readPEM(reader);
        cert.storePEM("/var/tmp/" + username + "cert.pem");
        
        System.out.println();
        System.out.println("openssl x509 -text -noout -in /var/tmp/" + username + "cert.pem");
        
    }

    public static String getCertificate(StringBuffer response) {
        String pemCert= null;
        int start= response.indexOf("<Certificate>");
        if (start != -1) {
            start+= "<Certificate>".length();
            int stop= response.indexOf("</Certificate>", start);
            if ( stop != -1) {
                pemCert= response.substring(start,stop);
            }
            else {
                System.err.println("</Certificate> not found!");
            }
        }
        else {
            System.err.println("<Certificate> not found!");
        }
        return pemCert;

    }

    public static String getDN(StringBuffer response) {
        String dn= null;
        int start= response.indexOf("<DN>");
        if (start != -1) {
            start+= "<DN>".length();
            int stop= response.indexOf("</DN>", start);
            if ( stop != -1) {
                dn= response.substring(start,stop);
            }
            else {
                System.err.println("</DN> not found!");
            }
        }
        else {
            System.err.println("<DN> not found!");
        }
        return dn;
    }

    public static String getAuthorizationToken(StringBuffer response) {
        String authToken= null;
        int start= response.indexOf("<AuthorizationToken>");
        if (start != -1) {
            start+= "<AuthorizationToken>".length();
            int stop= response.indexOf("</AuthorizationToken>", start);
            if ( stop != -1) {
                authToken= response.substring(start,stop);
            }
            else {
                System.err.println("</AuthorizationToken> not found!");
            }
        }
        else {
            System.err.println("<AuthorizationToken> not found!");
        }
        return authToken;
    }

    public static StringBuffer getContent(InputStream is) throws IOException {
        InputStreamReader isr= new InputStreamReader(is);
        BufferedReader in= new BufferedReader(isr);
        StringBuffer html= new StringBuffer();
        String line= null;
        while ((line= in.readLine()) != null) {
            html.append(line).append("\n");
        }
        return html;

    }

    public static void dumpResponse(InputStream is) throws IOException {
        System.out.println("---RESPONSE BEGIN---");
        System.out.print(getContent(is));
        System.out.println("---RESPONSE END---");

    }
}
