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
package org.glite.slcs.shibclient.metadata;

/**
 * IdentityProvider is a Shibboleth IdP description
 *
 * @author Valery Tschopp <tschopp@switch.ch>
 * @version $Revision: 1.3 $
 */
public class IdentityProvider extends Provider {

    public static final int SSO_AUTHTYPE_BASIC= 1;

    public static final int SSO_AUTHTYPE_NTLM= 2;

    public static final int SSO_AUTHTYPE_CAS= 3;

    public static final int SSO_AUTHTYPE_PUBCOOKIE= 4;
    
    public static final int SSO_AUTHTYPE_FORM= 5;

    // Shib entity ID
    private String entityID_;
    
    private int authType_;

    private String authUrl_;

    // description of FORM (if any)
    private String authFormName_= null;

    private String authFormUsername_= null;

    private String authFormPassword_= null;

    // description of BASIC or NTLM (if any)
    private String authRealm_= null;

    public IdentityProvider(String id, String name, String ssoUrl,
            String authTypeName, String authUrl) {
        super(id, name, ssoUrl);
        this.authUrl_= authUrl;
        this.authType_= getAuthType(authTypeName);
    }

    public IdentityProvider(String id, String name, String ssoUrl,
            int authType, String authUrl) {
        super(id, name, ssoUrl);
        this.authUrl_= authUrl;
        this.authType_= authType;
    }
    
    public void setEntityID(String entityID) {
        this.entityID_= entityID;
    }

    /**
     * @return <code>null</code> if the IdP doesn't support SAML2
     */
    public String getEntityID() {
        return entityID_;
    }
    static protected int getAuthType(String name) {
        if (name.equalsIgnoreCase("basic")) {
            return SSO_AUTHTYPE_BASIC;
        }
        else if (name.equalsIgnoreCase("ntlm")) {
            return SSO_AUTHTYPE_NTLM;
        }
        else if (name.equalsIgnoreCase("cas")) {
            return SSO_AUTHTYPE_CAS;
        }
        else if (name.equalsIgnoreCase("pubcookie")) {
            return SSO_AUTHTYPE_PUBCOOKIE;
        }
        else if (name.equalsIgnoreCase("form")) {
            return SSO_AUTHTYPE_FORM;
        }

        return 0;
    }

    public String getAuthTypeName() {
        String authTypeName= "UNKNOWN";
        switch (this.authType_) {
        case SSO_AUTHTYPE_BASIC:
            authTypeName= "BASIC";
            break;
        case SSO_AUTHTYPE_NTLM:
            authTypeName= "NTLM";
            break;
        case SSO_AUTHTYPE_CAS:
            authTypeName= "CAS";
            break;
        case SSO_AUTHTYPE_PUBCOOKIE:
            authTypeName= "PUBCOOKIE";
            break;
        case SSO_AUTHTYPE_FORM:
            authTypeName= "FORM";
            break;
        default:
            break;
        }
        return authTypeName;
    }

    /**
     * @return the authentication url.
     */
    public String getAuthUrl() {
        return this.authUrl_;
    }

    /**
     * @return the authentication type.
     */
    public int getAuthType() {
        return this.authType_;
    }

    /**
     * @return Returns the authFormName.
     */
    public String getAuthFormName() {
        return this.authFormName_;
    }

    /**
     * @param authFormName
     *            The authFormName to set.
     */
    public void setAuthFormName(String authFormName) {
        this.authFormName_= authFormName;
    }

    /**
     * @return Returns the authFormPassword.
     */
    public String getAuthFormPassword() {
        return this.authFormPassword_;
    }

    /**
     * @param authFormPassword
     *            The authFormPassword to set.
     */
    public void setAuthFormPassword(String authFormPassword) {
        this.authFormPassword_= authFormPassword;
    }

    /**
     * @return Returns the authFormUsername.
     */
    public String getAuthFormUsername() {
        return this.authFormUsername_;
    }

    /**
     * @param authFormUsername
     *            The authFormUsername to set.
     */
    public void setAuthFormUsername(String authFormUsername) {
        this.authFormUsername_= authFormUsername;
    }

    /**
     * @return Returns the authRealm.
     */
    public String getAuthRealm() {
        return this.authRealm_;
    }

    /**
     * @param authRealm
     *            The authRealm to set.
     */
    public void setAuthRealm(String authRealm) {
        this.authRealm_= authRealm;
    }

    protected StringBuffer toStringBuffer() {
        StringBuffer sb= new StringBuffer();
        if (entityID_!=null) {
            sb.append('(').append(entityID_).append(')');
        }
        sb.append(super.toStringBuffer());
        sb.append(",[");
        sb.append(this.authType_).append(",");
        sb.append(this.authUrl_);
        if (this.authFormName_ != null) {
            sb.append(",").append("form=").append(this.authFormName_);
        }
        if (this.authFormUsername_ != null) {
            sb.append(",").append("field=").append(this.authFormUsername_);
        }
        if (this.authFormPassword_ != null) {
            sb.append(",").append("field=").append(this.authFormPassword_);
        }
        if (this.authRealm_ != null) {
            sb.append(",").append("realm=").append(this.authRealm_);
        }
        sb.append("]");
        return sb;
    }

}
