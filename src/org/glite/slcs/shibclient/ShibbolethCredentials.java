/*
 * $Id: ShibbolethCredentials.java,v 1.1 2006/10/24 09:24:18 vtschopp Exp $
 * 
 * Created on May 31, 2006 by tschopp
 *
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */
package org.glite.slcs.shibclient;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;

/**
 * ShibbolethCredentials are credential for a Shibboleth IdP
 *
 * @author tschopp
 * @version $Revision: 1.1 $
 */
public class ShibbolethCredentials extends UsernamePasswordCredentials
        implements Credentials {

    private String identityProviderID_= null;

    /**
     * Constructor
     * @param username
     * @param password
     * @param identityProviderID
     */
    public ShibbolethCredentials(String username, String password,
            String identityProviderID) {
        super(username, password);
        this.identityProviderID_= identityProviderID;
    }
    
    /**
     * Constructor
     * @param username
     * @param password
     * @param identityProviderID
     */
    public ShibbolethCredentials(String username, char[] password,
            String identityProviderID) {
        this(username, new String(password), identityProviderID);
    }
    

    /**
     * @return The IdP providerID
     */
    public String getIdentityProviderID() {
        return this.identityProviderID_;
    }
    
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb= new StringBuffer();
        sb.append("ShibbolethCredentials[");
        sb.append(this.getUserName()).append(':');
        int length= this.getPassword().length();
        for (int i= 0; i < length; i++) {
            sb.append('*');
        }
        sb.append(',');
        sb.append(this.getIdentityProviderID());
        sb.append(']');
        return sb.toString();
    }

}
