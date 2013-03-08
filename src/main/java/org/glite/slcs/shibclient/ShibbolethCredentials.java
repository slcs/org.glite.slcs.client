/*
 * Copyright (c) 2010-2013 SWITCH
 * Copyright (c) 2006-2010 Members of the EGEE Collaboration
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
