/*
 * $Id: ShibbolethClient.java,v 1.9 2008/04/25 11:47:39 vtschopp Exp $
 * 
 * Created on Jul 5, 2006 by tschopp
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.AuthState;
import org.apache.commons.httpclient.cookie.CookieSpecBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glite.slcs.AuthException;
import org.glite.slcs.RemoteException;
import org.glite.slcs.SLCSConfigurationException;
import org.glite.slcs.ServiceException;
import org.glite.slcs.UnknownResourceException;
import org.glite.slcs.jericho.html.Element;
import org.glite.slcs.jericho.html.FormControl;
import org.glite.slcs.jericho.html.FormControlType;
import org.glite.slcs.jericho.html.Source;
import org.glite.slcs.jericho.html.Tag;
import org.glite.slcs.shibclient.metadata.IdentityProvider;
import org.glite.slcs.shibclient.metadata.ShibbolethClientMetadata;

/**
 * ShibbolethClient is a SSO login parser and a Shibboleth Browser/POST
 * (SAML/POST) or SAML/Artifact profile handler.
 * <p>
 * <b>This code is just a hack. Don't rely on it to develop something else.</b>
 * 
 * @author Valery Tschopp <tschopp@switch.ch>
 * @version $Revision: 1.9 $
 */
public class ShibbolethClient {

    /** Logging */
    private static final Log LOG = LogFactory.getLog(ShibbolethClient.class);

    /** The HttpClient delegate */
    private HttpClient httpClient_;

    /** Shibboleth client metadata */
    private ShibbolethClientMetadata metadata_;

    /** Shibboleth client credentials */
    private ShibbolethCredentials credentials_;

    /** authenticated or not */
    private boolean isAuthenticated_ = false;

    /**
     * Creates a Shibboleth client wrapping a {@link HttpClient}. Requires the
     * Shibboleth client metadata and the Shibboleth credentials.
     * 
     * @param client
     *            The HttpClient engine.
     * @param metadata
     *            The Shibboleth client metadata
     * @param credentials
     *            The Shibboleth credentials (username and password)
     * @throws SLCSConfigurationException
     */
    public ShibbolethClient(HttpClient client,
            ShibbolethClientMetadata metadata, ShibbolethCredentials credentials)
            throws SLCSConfigurationException {
        // check consistancy of creds and metadata
        String id = credentials.getIdentityProviderID();
        IdentityProvider idp = metadata.getIdentityProvider(id);
        if (idp == null) {
            LOG.error("Unknown IdP " + id);
            throw new SLCSConfigurationException(
                    "IdP providerId: "
                            + id
                            + " is not defined in metadata file.\nPlease run the 'slcs-info' command.");
        }

        this.httpClient_ = client;
        this.metadata_ = metadata;
        this.credentials_ = credentials;

    }

    /**
     * Authenticates the user with his IdP for the default SLCS SP.
     * 
     * @return <code>true</code> iff the user have been authenticated by his
     *         Identity Provider.
     * @throws ServiceException
     * @throws RemoteException
     * @throws AuthException
     * @throws UnknownResourceException
     */
    public boolean authenticate() throws AuthException, RemoteException,
            ServiceException, UnknownResourceException {
        String slcsEntryURL = metadata_.getSLCS().getUrl();
        return authenticate(slcsEntryURL);
    }

    /**
     * Authenticates the user on his IdP for the given SP.
     * 
     * @param spEntryURL
     *            The Service Provider entry point URL
     * @return <code>true</code> iff the user have been authenticated by his
     *         Identity Provider.
     * @throws AuthException
     * @throws RemoteException
     * @throws ServiceException
     * @throws UnknownResourceException
     */
    public boolean authenticate(String spEntryURL) throws AuthException,
            RemoteException, ServiceException, UnknownResourceException {

        String idpProviderID = credentials_.getIdentityProviderID();
        IdentityProvider idp = metadata_.getIdentityProvider(idpProviderID);
        if (idp == null) {
            throw new UnknownResourceException("IdP " + idpProviderID
                    + " not found in Metadata");
        }
        LOG.info(spEntryURL + " IdP=" + idp.getUrl() + " AuthType="
                + idp.getAuthTypeName());
        String idpURL = idp.getUrl();
        LOG.debug("idpURL=" + idpURL);
        // LOG.debug("wayfURL=" + wayfURL);
        try {
            URI idpSSOResponseURI = null;

            // 1. get the first redirection or already authenticated
            URI spLoginResponseURI = processSPEntry(spEntryURL);

            // either wayf or idp or same (already authenticated)
            LOG.debug("spLoginResponseURI=" + spLoginResponseURI);
            String spLoginResponseURL = spLoginResponseURI.getEscapedURI();
            // check if already authenticated
            if (spLoginResponseURL.startsWith(spEntryURL)) {
                LOG.info("Already authenticated? " + isAuthenticated_ + ": "
                        + spLoginResponseURL);
                return this.isAuthenticated_;
            }
            // checks if URL contains the shire= parameter (WAYF of IdP)
            if (spLoginResponseURL.indexOf("shire=") == -1) {
                LOG.error("Unexpected spLoginResponseURL=" + spLoginResponseURL);
                throw new ServiceException(
                        "SP response URL doesn't contain the 'shire=' parameter: "
                                + spLoginResponseURL);
            }
            else {
                LOG.debug("IdP SSO: " + idpURL);
                // 2. process the IdP SSO login
                idpSSOResponseURI = processIdPSSO(idp, spLoginResponseURI);
            }

            // 3. process the IdP SSO response -> Artifact or Browser/POST
            // profile
            URI idpResponseURI = processIdPSSOResponse(idp, idpSSOResponseURI);
            String url = idpResponseURI.getURI();
            if (url.equals(spEntryURL)) {
                this.isAuthenticated_ = true;
                LOG.info("Sucessful authentication");
            }

        } catch (URIException e) {
            LOG.error("URIException: " + e);
            e.printStackTrace();
        } catch (HttpException e) {
            LOG.error("HttpException: " + e);
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("IOException: " + e);
            e.printStackTrace();
            throw new RemoteException(e);
        }
        return this.isAuthenticated_;
    }

    /**
     * Processes the response of the IdP SSO and dispatches to the Browser/POST
     * or the Artificate processor.
     * 
     * @param idp
     *            The {@link IdentityProvider}.
     * @param idpSSOResponseURI
     *            The IdP SSO response {@link URI}.
     * @return
     * @throws RemoteException
     */
    private URI processIdPSSOResponse(IdentityProvider idp,
            URI idpSSOResponseURI) throws HttpException, IOException,
            RemoteException {
        URI idpResponseURI = null;

        String idpSSOResponseURL = idpSSOResponseURI.getURI();
        GetMethod getIdPSSOResponseMethod = new GetMethod(idpSSOResponseURL);

        // set Pubcookie cookie request header: Cookie: <pubcookie_pre_s>;
        // <pubcookie_g>
        if (idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_PUBCOOKIE) {
            Cookie pubcookie_pre_s = getCookie("pubcookie_pre_s");
            Cookie pubcookie_g = getCookie("pubcookie_g");
            String pubcookies = pubcookie_pre_s.toString() + "; "
                    + pubcookie_g.toString();
            LOG.debug("setting PubCookie request Cookie: " + pubcookies);
            getIdPSSOResponseMethod.addRequestHeader("Cookie", pubcookies);
        }

        // BUG FIX: check if it have a SAML/Artifact endpoint
        boolean isPossiblyUsingArtifact = false;
        if (idpSSOResponseURL.indexOf("/SAML/Artifact") != -1) {
            isPossiblyUsingArtifact = true;
            LOG.info("The SP possibly try to use a SAML/Artifact profile");
        }

        LOG.info("GET IdPSSOResponse: " + idpSSOResponseURL);
        int idpSSOResponseStatus = executeMethod(getIdPSSOResponseMethod);
        LOG.debug(getIdPSSOResponseMethod.getStatusLine());

        // SAML/Artifact are already processed.
        if (isPossiblyUsingArtifact) {
            // check if there is a loop. if so the SSO failed for some reason...
            if (idpSSOResponseURI.equals(getIdPSSOResponseMethod.getURI())) {
                String htmlBody = inputStreamToString(getIdPSSOResponseMethod.getResponseBodyAsStream());
                LOG.error("Something went wrong with the IdP SAML/Artifact response: "
                        + htmlBody);
                throw new RemoteException(
                        "The Identity Provider SAML/Artifact response failed: "
                                + idp.getUrl() + ". Please see the log file.");
            }
            idpResponseURI = getIdPSSOResponseMethod.getURI();
        }
        else {
            // try to parse the Browser/POST profile in the HTML source
            InputStream htmlStream = getIdPSSOResponseMethod.getResponseBodyAsStream();
            idpResponseURI = processIdPBrowserPOST(idp, idpSSOResponseURI, htmlStream);
        }

        LOG.debug("getIdPSSOResponseMethod.releaseConnection()");
        getIdPSSOResponseMethod.releaseConnection();

        return idpResponseURI;
    }

    /**
     * Fully reads an {@link InputStream} into a {@link String}.
     * 
     * @param is
     *            The InputStream to read.
     * @return The context of the input stream.
     */
    private String inputStreamToString(InputStream is) {
        StringBuffer sb = new StringBuffer(1024);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            char[] chars = new char[1024];
            while (reader.read(chars) > -1) {
                sb.append(chars);
                chars = new char[1024];
            }
            reader.close();
        } catch (IOException e) {
            LOG.error(e);
        }
        return sb.toString();
    }

    /**
     * Processes the IdP response as a Browser/POST
     * 
     * @param idp
     *            The {@link IdentityProvider}.
     * @param idpSSOResponseURI
     *            The IdP SSO reponse {@link URI}.
     * @return the SP URI to go to
     * @throws RemoteException
     */
    private URI processIdPBrowserPOST(IdentityProvider idp,
            URI idpSSOResponseURI, InputStream htmlStream)
            throws RemoteException {
        // return value
        URI browserPostResponseURI = null;
        RemoteException remoteException = null;

        try {
            Source source = new Source(htmlStream);
            List<Element> forms = source.findAllElements(Tag.FORM);
            if (!forms.isEmpty()) {
                // check if form contains a valid SAML Browser/POST
                for (Element form : forms) {
                    String spSAMLURL = form.getAttributeValue("ACTION");
                    LOG.debug("SAML Browser/POST URL=" + spSAMLURL);
                    if (spSAMLURL == null) {
                        // no SAML post URL found
                        String htmlBody = inputStreamToString(htmlStream);
                        LOG.error("No SAML Browser/POST FORM ACTION found: "
                                + idpSSOResponseURI + ": "
                                + htmlBody);
                        remoteException = new RemoteException(
                                "No SAML Browser/POST FORM ACTION found: "
                                        + idpSSOResponseURI
                                        + ". Please see the log file.");

                        break; // exit loop
                    }

                    // create POST method
                    PostMethod postSPSAMLMethod = new PostMethod(spSAMLURL);
                    // add all HIDDEN fields to POST
                    List<FormControl> formControls = form.findFormControls();
                    for (FormControl control : formControls) {
                        FormControlType type = control.getFormControlType();
                        if (type.equals(FormControlType.HIDDEN)) {
                            String name = control.getName();
                            Collection<CharSequence> values = control.getValues();
                            for (CharSequence value : values) {
                                LOG.debug("HIDDEN " + name + "=" + value);
                                // add all hidden fields
                                postSPSAMLMethod.addParameter(name,
                                        (String) value);
                            }
                        }
                    }

                    // execute the SAML post
                    LOG.info("POST SPSAMLMethod: " + postSPSAMLMethod.getURI());
                    int spSAMLResponseStatus = executeMethod(postSPSAMLMethod);
                    LOG.debug(postSPSAMLMethod.getStatusLine());

                    // status must be 302 and redirect Location
                    Header location = postSPSAMLMethod.getResponseHeader("Location");
                    if (spSAMLResponseStatus == 302 && location != null) {
                        String url = location.getValue();
                        browserPostResponseURI = new URI(url, false);
                        LOG.debug("Redirect: " + browserPostResponseURI);

                    }
                    else {
                        LOG.error("Unexpected SP response: Status="
                                + spSAMLResponseStatus + " Location="
                                + location);
                        remoteException = new RemoteException(
                                "Unexpected SP response: Status="
                                        + spSAMLResponseStatus + " Location="
                                        + location);
                    }

                    LOG.trace("postSPSAMLMethod.releaseConnection()");
                    postSPSAMLMethod.releaseConnection();

                } // forms loop
            }
            else {
                // no SAML post found
                String htmlBody = inputStreamToString(htmlStream);
                LOG.error("No SAML Browser/POST profile found: "
                        + idpSSOResponseURI + ": " + htmlBody);
                remoteException = new RemoteException(
                        "No SAML Browser/POST profile found: "
                                + idpSSOResponseURI
                                + ". Please see the log file.");
            }


        } catch (URIException e) {
            e.printStackTrace();
            remoteException = new RemoteException(e.getMessage(), e);
        } catch (IOException e) {
            e.printStackTrace();
            remoteException = new RemoteException(e.getMessage(), e);
        }

        if (browserPostResponseURI == null) {
            if (remoteException != null) {
                throw remoteException;
            }
        }

        return browserPostResponseURI;
    }

    /**
     * Gets the Shibboleth session needed afterward.
     * 
     * @param entryURL
     * @return
     * @throws URIException
     * @throws HttpException
     * @throws IOException
     * @throws RemoteException
     */
    private URI processSPEntry(String entryURL) throws URIException,
            HttpException, IOException, RemoteException {
        GetMethod getSPEntryMethod = new GetMethod(entryURL);
        LOG.info("GET SPEntryMethod: " + getSPEntryMethod.getURI());
        int spEntryStatus = executeMethod(getSPEntryMethod);
        LOG.debug(getSPEntryMethod.getStatusLine());

        URI loginResponseURI = getSPEntryMethod.getURI();

        LOG.trace("getSPEntryMethod.releaseConnection()");
        getSPEntryMethod.releaseConnection();

        // check response status
        if (spEntryStatus != 200) {
            throw new RemoteException("Unexpected SP response status:"
                    + spEntryStatus + " " + entryURL);
        }
        return loginResponseURI;
    }

    /**
     * @param idp
     * @param query
     * @throws URIException
     * @throws HttpException
     * @throws IOException
     * @throws AuthException
     * @throws RemoteException
     * @throws ServiceException
     */
    private URI processIdPSSO(IdentityProvider idp, URI spResponseURI)
            throws URIException, HttpException, IOException, AuthException,
            RemoteException, ServiceException {
        String idpSSOURL = idp.getUrl();
        String query = spResponseURI.getQuery();
        if (query != null) {
            idpSSOURL += "?" + query;
        }
        // create HttpMethod
        GetMethod getIdpSSOMethod = new GetMethod(idpSSOURL);

        // set credential for basic or ntlm
        int authType = idp.getAuthType();
        LOG.debug("IdP authType=" + idp.getAuthTypeName());
        if (authType == IdentityProvider.SSO_AUTHTYPE_BASIC
                || authType == IdentityProvider.SSO_AUTHTYPE_NTLM) {
            // enable BASIC or NTLM authN
            LOG.info("Enable " + idp.getAuthTypeName() + " authentication: "
                    + credentials_);
            URI idpSSOURI = getIdpSSOMethod.getURI();
            AuthScope scope = new AuthScope(idpSSOURI.getHost(), 443,
                    idp.getAuthRealm());
            LOG.debug("AuthScope=" + scope);
            httpClient_.getState().setCredentials(scope, credentials_);
            getIdpSSOMethod.setDoAuthentication(true);
        }

        // execute the method
        LOG.info("GET IdpSSOMethod: " + getIdpSSOMethod.getURI());
        int idpSSOResponseStatus = executeMethod(getIdpSSOMethod);
        LOG.debug(getIdpSSOMethod.getStatusLine());

        URI idpSSOResponseURI = getIdpSSOMethod.getURI();
        String idpSSOResponseQuery = idpSSOResponseURI.getEscapedQuery();
        LOG.debug("idpSSOResponseURI=" + idpSSOResponseURI);
        LOG.debug("idpSSOResponseQuery=" + idpSSOResponseQuery);

        // XXX
        dumpHttpClientCookies();

        LOG.debug("idpSSOResponseStatus=" + idpSSOResponseStatus);
        // if BASIC or NTLM failed
        if (idpSSOResponseStatus == 401) {
            LOG.error("BASIC or NTLM authentication was required for "
                    + idpSSOURL);

            LOG.trace("GETIdpSSOMethod.releaseConnection()");
            getIdpSSOMethod.releaseConnection();

            if (LOG.isDebugEnabled()) {
                AuthState authState = getIdpSSOMethod.getHostAuthState();
                String realm = authState.getRealm();
                AuthScheme scheme = authState.getAuthScheme();
                String schemeName = scheme.getSchemeName();
                LOG.debug("Auth realm=" + realm + " scheme=" + schemeName);
            }
            throw new AuthException(idp.getAuthTypeName()
                    + " Authentication failed: " + this.credentials_
                    + " Status: " + idpSSOResponseStatus);
        }
        // CAS sends 200 + Cookies and the login form directly
        else if (idpSSOResponseStatus == 200
                && idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_CAS) {
            LOG.debug("Process CAS login form...");
            // process CAS login form
            InputStream idpLoginForm = getIdpSSOMethod.getResponseBodyAsStream();
            idpSSOResponseURI = processIdPLoginForm(idp, idpSSOResponseURI,
                    idpLoginForm);
            LOG.debug("CAS idpSSOResponseURI=" + idpSSOResponseURI);
        }
        // PUBCOOKIE sends 200 + Cookies and a HTML page #!@!
        // <meta http-equiv="Refresh"
        // content="0;URL=https://aai1.unil.ch/"> back
        else if (idpSSOResponseStatus == 200
                && idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_PUBCOOKIE) {

            LOG.trace("GETIdpSSOMethod.releaseConnection()");
            getIdpSSOMethod.releaseConnection();

            LOG.debug("Get Pubcookie login form...");

            GetMethod getPubcookieLoginFormMethod = new GetMethod(
                    idp.getAuthUrl());
            // set Cookie 'pubcookie_g_req'
            Cookie pubcookie_r_req = getCookie("pubcookie_g_req");
            getPubcookieLoginFormMethod.setRequestHeader("Cookie",
                    pubcookie_r_req.toString());
            LOG.info("GET PubcookieLoginFormMethod: "
                    + getPubcookieLoginFormMethod.getURI());
            int pubcookieLoginFormStatus = executeMethod(getPubcookieLoginFormMethod);
            LOG.debug(getPubcookieLoginFormMethod.getStatusLine());
            // TODO check status

            // XXX
            dumpHttpClientCookies();

            // process pubcookie login form
            InputStream loginFormStream = getPubcookieLoginFormMethod.getResponseBodyAsStream();
            idpSSOResponseURI = processIdPLoginForm(idp, idpSSOResponseURI,
                    loginFormStream);
            LOG.debug("Pubcookie idpSSOResponseURI=" + idpSSOResponseURI);

            LOG.debug("getPubcookieLoginFormMethod.releaseConnection()");
            getPubcookieLoginFormMethod.releaseConnection();

        }

        else {
            // TODO: error handling
            LOG.error("Unexpected Status: " + idpSSOResponseStatus
                    + " AuthType: " + idp.getAuthTypeName());
        }

        LOG.debug("GETIdpSSOMethod.releaseConnection()");
        getIdpSSOMethod.releaseConnection();

        return idpSSOResponseURI;

    }

    /**
     * Prases and process Pubcookie or CAS login form.
     * 
     * @param idp
     * @param htmlForm
     * @throws IOException
     * @throws RemoteException
     * @throws ServiceException
     * @throws AuthException
     */
    private URI processIdPLoginForm(IdentityProvider idp, URI ssoLoginURI,
            InputStream htmlForm) throws IOException, RemoteException,
            ServiceException, AuthException {
        LOG.info("Parse and process " + idp.getAuthTypeName()
                + " login form...");

        boolean formFound = false;
        URI idpLoginFormResponseURI = null;

        // Parse the FORM with Jericho HTML Parser
        Source source = new Source(htmlForm);
        List<Element> forms = source.findAllElements(Tag.FORM);
        for (Element form : forms) {
            String formName = form.getAttributeValue("NAME");
            // BUG FIX: UniL use a CAS login form with NO NAME defined.
            // first try with the form ID as NAME, otherwise use an empty name.
            // the metadata should also define an empty name for this particular
            // form.
            LOG.debug("FORM NAME: " + formName);
            if (formName == null) {
                LOG.warn("form have no NAME, try form ID...");
                String formId = form.getAttributeValue("ID");
                if (formId == null) {
                    LOG.warn("form have no NAME and no ID, using empty name...");
                    formName = "";
                }
                else {
                    formName = formId;
                }
            }

            if (formName.equals(idp.getAuthFormName())) {
                formFound = true;
                String formLocation = form.getAttributeValue("ACTION");
                if (formLocation == null || formLocation.equals("")) {
                    // no form location to POST
                    formLocation = ssoLoginURI.getEscapedURI();
                    LOG.info("form ACTION URL=" + formLocation);
                }
                else {
                    // BUG FIX: if CAS3 doesn't use cookie, then it adds a path
                    // component ;jsessionid=... between the URL and the query
                    // parameters (?) i.e.:
                    // action="login;jsessionid=52E28683EF109486FAB652E3D76EEDDE?param1..."
                    int jsessionIdPos = formLocation.indexOf(";jsessionid=");
                    int questionMarkPos = formLocation.indexOf('?');
                    if (jsessionIdPos != -1 && jsessionIdPos < questionMarkPos) {
                        LOG.warn("form ACTION URL contains ';jsessionid=...': "
                                + formLocation);
                        // extract the ;jsessionid= from the form action url
                        String jsessionId = formLocation.substring(
                                jsessionIdPos, questionMarkPos);
                        LOG.debug("jsessionId=" + jsessionId);
                        // and add it to the existing path
                        String path = ssoLoginURI.getPath();
                        ssoLoginURI.setPath(path + jsessionId);
                        formLocation = ssoLoginURI.getEscapedURI();
                        LOG.info("corrected form ACTION URL=" + formLocation);
                    }
                }

                String formMethod = form.getAttributeValue("METHOD");
                LOG.debug("FORM name=" + formName + " location=" + formLocation
                        + " method=" + formMethod);

                if (!formLocation.equals("")
                        && formMethod.equalsIgnoreCase("POST")) {

                    PostMethod postLoginFormMethod = new PostMethod(
                            formLocation);

                    // add all HIDDEN fields to POST
                    List<FormControl> formControls = form.findFormControls();
                    for (FormControl control : formControls) {
                        FormControlType type = control.getFormControlType();
                        if (type.equals(FormControlType.HIDDEN)) {
                            String name = control.getName();
                            Collection<String> values = control.getValues();
                            for (String value : values) {
                                LOG.debug("HIDDEN " + name + "=" + value);
                                // add all hidden fields
                                postLoginFormMethod.addParameter(name, value);
                            }
                        }
                    }
                    // add username field
                    postLoginFormMethod.addParameter(idp.getAuthFormUsername(),
                            this.credentials_.getUserName());
                    // add the PASSWORD field
                    postLoginFormMethod.addParameter(idp.getAuthFormPassword(),
                            this.credentials_.getPassword());

                    // execute the login POST
                    LOG.info("POST LoginFormMethod: "
                            + postLoginFormMethod.getURI());
                    // XXX
                    dumpHttpClientCookies();
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Host:"
                                + postLoginFormMethod.getURI().getHost());
                        LOG.trace("Path:"
                                + postLoginFormMethod.getURI().getPath());
                    }
                    // BUG FIX: set the CAS JSESSIONID cookie by hand.
                    // default CookiePolicy is RFC2109 and doesn't handle
                    // correctly FQDN domain.
                    String host = postLoginFormMethod.getURI().getHost();
                    String path = postLoginFormMethod.getURI().getPath();
                    Cookie cookies[] = getMatchingCookies("JSESSIONID", host,
                            path);
                    for (int j = 0; j < cookies.length; j++) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("setting CAS request Cookie: "
                                    + cookies[j]);
                        }
                        postLoginFormMethod.setRequestHeader("Cookie",
                                cookies[j].toString());
                    }

                    int formLoginResponseStatus = executeMethod(postLoginFormMethod);
                    LOG.debug(postLoginFormMethod.getStatusLine());

                    // XXX
                    // dumpHttpClientCookies();

                    // CAS send a 302 + Location header back
                    if (formLoginResponseStatus == 302
                            && idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_CAS) {
                        LOG.debug("Process CAS response...");
                        Header location = postLoginFormMethod.getResponseHeader("Location");
                        if (location != null) {
                            String locationURL = location.getValue();
                            LOG.debug("CAS Redirect: " + locationURL);
                            // XXX: if location path (/cas/login) is not the IdP
                            // SSO path (/shibboleth-idp/SSO), then it's a wrong
                            // login
                            URI locationURI = new URI(locationURL, false);
                            String locationPath = locationURI.getPath();
                            String idpSSOURL = idp.getUrl();
                            URI idpSSOURI = new URI(idpSSOURL, false);
                            String idpSSOPath = idpSSOURI.getPath();
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("location path: " + locationPath);
                                LOG.debug("IdP SSO path: " + idpSSOPath);
                            }
                            if (!locationPath.equals(idpSSOPath)) {
                                LOG.error("CAS response is not the SSO url ("
                                        + idpSSOURL + "): " + locationURL);
                                throw new AuthException(idp.getAuthTypeName()
                                        + " Authentication failed: "
                                        + this.credentials_);
                            }
                            idpLoginFormResponseURI = new URI(locationURL,
                                    false);
                        }
                        else {
                            LOG.error("CAS send status 302 but no redirect Location header");
                            throw new AuthException(idp.getAuthTypeName()
                                    + " Authentication failed: "
                                    + this.credentials_);
                        }
                    }
                    // Pubcookie send 200 + fucking HTML meta-data refresh!!!
                    // <meta http-equiv="Refresh"
                    // content="0;URL=https://cipher.ethz.ch/login/index.cgi">
                    else if (formLoginResponseStatus == 200
                            && idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_PUBCOOKIE) {
                        LOG.debug("Process Pubcookie response...");
                        String pubcookieRefreshURL = null;
                        InputStream pubcookieResponse = postLoginFormMethod.getResponseBodyAsStream();
                        Source pubcookieSource = new Source(pubcookieResponse);
                        List<Element> metas = pubcookieSource.findAllElements(Tag.META);
                        for (Element meta : metas) {
                            LOG.debug("Pubcookie META: " + meta);
                            String metaRefresh = meta.getAttributeValue("HTTP-EQUIV");
                            if (metaRefresh != null
                                    && metaRefresh.equalsIgnoreCase("Refresh")) {
                                String metaContent = meta.getAttributeValue("CONTENT");
                                // format: <n>;URL=<location> where <n> in
                                // second
                                int pos = metaContent.indexOf(";URL=");
                                if (pos > 0) {
                                    pubcookieRefreshURL = metaContent.substring(pos + 5);
                                }
                            }
                        }

                        if (pubcookieRefreshURL != null) {
                            LOG.debug("Pubcookie refresh: "
                                    + pubcookieRefreshURL);
                            idpLoginFormResponseURI = new URI(
                                    pubcookieRefreshURL, false);
                        }
                        else {
                            LOG.error("Pubcookie META Refresh not found");
                            throw new AuthException(idp.getAuthTypeName()
                                    + " Authentication failed: "
                                    + this.credentials_);
                        }

                        // XXX
                        dumpHttpClientCookies();

                    }
                    else {
                        LOG.error("Unexpected response status: "
                                + formLoginResponseStatus + " AuthType:"
                                + idp.getAuthTypeName());
                        throw new AuthException(idp.getAuthTypeName()
                                + " Authentication failed: "
                                + this.credentials_);
                    }

                    LOG.debug("POSTLoginFormMethod.releaseConnection()");
                    postLoginFormMethod.releaseConnection();
                }
            }
        } // end while

        if (!formFound) {
            LOG.error("FORM name=" + idp.getAuthFormName() + " not found");
            throw new ServiceException("FORM name=" + idp.getAuthFormName()
                    + " not found");
        }

        return idpLoginFormResponseURI;

    }

    /**
     * @return isAuthenticated
     */
    public boolean isAuthenticated() {
        return this.isAuthenticated_;
    }

    /**
     * Delegates execution of the HttpMethod to the underlying HttpClient.
     * 
     * @param method
     *            The HttpMethod to execute.
     * @return The method's response code.
     * @throws HttpException
     *             If an I/O (transport) error occurs.
     * @throws IOException
     *             If a protocol exception occurs.
     */
    public int executeMethod(HttpMethod method) throws HttpException,
            IOException {
        // use delegate
        return this.httpClient_.executeMethod(method);
    }

    private void dumpHttpClientCookies() {
        if (LOG.isDebugEnabled()) {
            Cookie[] cookies = this.httpClient_.getState().getCookies();
            StringBuffer sb = new StringBuffer();
            sb.append("\n");
            sb.append("CookiePolicy=").append(
                    httpClient_.getParams().getCookiePolicy()).append("\n");
            sb.append("---HTTPCLIENT COOKIES BEGIN---").append("\n");
            for (int i = 0; i < cookies.length; i++) {
                String path = cookies[i].getPath();
                String domain = cookies[i].getDomain();
                boolean secure = cookies[i].getSecure();
                // sb.append(name).append('=').append(value).append("\n");
                sb.append(i).append(": ").append(cookies[i]);
                sb.append(" domain:").append(domain);
                sb.append(" path:").append(path);
                sb.append(" secure:").append(secure);
                sb.append("\n");
            }
            sb.append("---HTTPCLIENT COOKIES END---");
            LOG.debug(sb.toString());
        }
    }

    /**
     * Returns the <b>first</b> Cookie or <code>null</code> if not exists.
     * 
     * @param name
     *            The Cookie name
     * @return The cookie or <code>null</code>
     */
    private Cookie getCookie(String name) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cookie name=" + name);
        }
        Cookie[] cookies = this.httpClient_.getState().getCookies();
        for (int i = 0; i < cookies.length; i++) {
            String cookieName = cookies[i].getName();
            if (name.equals(cookieName)) {
                return cookies[i];
            }
        }
        return null;
    }

    private Cookie[] getMatchingCookies(String name, String host, String path) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("search Cookie matching name:" + name + " host:" + host
                    + " path:" + path);
        }
        List<Cookie> matchingCookies = new ArrayList<Cookie>();
        Cookie[] cookies = this.httpClient_.getState().getCookies();
        CookieSpecBase cookieSpecBase = new CookieSpecBase();
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookieSpecBase.match(host, 443, path, true, cookie)) {
                if (cookie.getName().equals(name)) {
                    LOG.debug("Cookie " + cookie + " matched");
                    matchingCookies.add(cookie);
                }
            }
        }
        return (Cookie[]) matchingCookies.toArray(new Cookie[matchingCookies.size()]);
    }

}
