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
