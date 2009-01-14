/*
 * $Id: Provider.java,v 1.2 2009/01/14 09:19:09 vtschopp Exp $
 * 
 * Created on Jul 6, 2006 by tschopp
 *
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */
package org.glite.slcs.shibclient.metadata;

/**
 * Provider is an abstract Shibboleth provider description (SP or IdP)
 *
 * @author Valery Tschopp <tschopp@switch.ch>
 * @version $Revision: 1.2 $
 */
public abstract class Provider implements Comparable<Provider> {

    private String name_;
    private String url_;
    private String id_;
    
    public Provider(String id, String name, String url) {
        this.id_= id;
        this.name_= name;
        this.url_= url;
    }

    public String getId() {
        return this.id_;
    }

    public String getName() {
        return this.name_;
    }

    public String getUrl() {
        return this.url_;
    }

    public String toString() {
        StringBuffer sb= new StringBuffer();
        sb.append(getClass().getName());
        sb.append("[");
        sb.append(this.toStringBuffer());
        sb.append("]");        
        return sb.toString();
    }
    
    protected StringBuffer toStringBuffer() {
        StringBuffer sb= new StringBuffer();
        sb.append(id_).append(",");
        sb.append(name_).append(",");
        sb.append(url_);
        return sb;
    }

    /**
     * Compare by Provider id.
     */
    public int compareTo(Provider p) {
        return id_.compareTo(p.getId());
    }
    
    
    
}
