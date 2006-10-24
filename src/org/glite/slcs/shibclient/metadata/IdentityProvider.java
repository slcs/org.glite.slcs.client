/*
 * $Id: IdentityProvider.java,v 1.1 2006/10/24 09:24:18 vtschopp Exp $
 * 
 * Created on Jul 6, 2006 by tschopp
 *
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */
package org.glite.slcs.shibclient.metadata;

/**
 * IdentityProvider is a Shibboleth IdP description
 *
 * @author Valery Tschopp <tschopp@switch.ch>
 * @version $Revision: 1.1 $
 */
public class IdentityProvider extends Provider {

    public static final int SSO_AUTHTYPE_BASIC= 1;

    public static final int SSO_AUTHTYPE_NTLM= 2;

    public static final int SSO_AUTHTYPE_CAS= 3;

    public static final int SSO_AUTHTYPE_PUBCOOKIE= 4;

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
        StringBuffer sb= super.toStringBuffer();
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
