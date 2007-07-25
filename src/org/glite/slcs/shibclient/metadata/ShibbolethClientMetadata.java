/*
 * Copyright (c) Members of the EGEE Collaboration. 2007.
 * See http://www.eu-egee.org/partners/ for details on the copyright
 * holders.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Version: $Id: ShibbolethClientMetadata.java,v 1.2 2007/07/25 15:38:51 vtschopp Exp $
 */
package org.glite.slcs.shibclient.metadata;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glite.slcs.SLCSConfigurationException;
import org.glite.slcs.config.SLCSClientConfiguration;
import org.glite.slcs.config.SLCSConfiguration;

/**
 * ShibbolethClientMetadata is the description of all Shibboleth
 * providers
 * 
 * @author Valery Tschopp <tschopp@switch.ch>
 * @version $Revision: 1.2 $
 */
public class ShibbolethClientMetadata extends SLCSConfiguration {

    /** Default providerId for the SLCS Server SP */
    static public final String DEFAULT_SLCS_PROVIDERID= "slcs";

    /** Log object for this class. */
    private static final Log LOG= LogFactory.getLog(ShibbolethClientMetadata.class);

    /** Metadata Entities */
    private Map providers_;

    private String slcsProviderId_= null;

    /**
     * 
     * @param filename
     * @throws SLCSConfigurationException
     */
    public ShibbolethClientMetadata(String filename)
            throws SLCSConfigurationException {
        super(filename);
        this.providers_= parseProviders();
    }

    /**
     * 
     * @param configuration
     * @throws SLCSConfigurationException
     */
    public ShibbolethClientMetadata(SLCSClientConfiguration configuration)
            throws SLCSConfigurationException {
        this(configuration.getFileConfiguration());
    }

    /**
     * 
     * @param configuration
     * @throws SLCSConfigurationException
     */
    public ShibbolethClientMetadata(FileConfiguration configuration)
            throws SLCSConfigurationException {
        super();
        setFileConfiguration(configuration);
        this.providers_= parseProviders();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.glite.slcs.config.SLCSConfiguration#checkConfiguration()
     */
    protected void checkConfiguration() throws SLCSConfigurationException {
        Configuration metadata= getConfiguration().subset("ShibbolethClientMetadata");
        if (metadata.isEmpty()) {
            String filename= getFilename();
            LOG.error("ShibbolethClientMetadata(" + filename
                    + "): Element ShibbolethClientMetadata missing");
            throw new SLCSConfigurationException("Element ShibbolethClientMetadata not defined or empty in: "
                    + filename);

        }
    }

    /**
     * 
     * @return
     * @throws SLCSConfigurationException
     */
    private Map parseProviders() throws SLCSConfigurationException {
        Map entities= new HashMap();
        LOG.debug("get configuration subset: ShibbolethClientMetadata");
        Configuration metadata= getConfiguration().subset("ShibbolethClientMetadata");
        // external metadata defined with filename= attribute?
        String metadataFilename= metadata.getString("[@filename]");
        if (metadataFilename != null) {
        	// load external metadata file       
        	try {
        		LOG.info("load external metadata: " + metadataFilename);
        		metadata = loadConfiguration(metadataFilename);
        	} catch (SLCSConfigurationException e) {
        		LOG.error("Failed to load external ShibbolethClientMetadata: " + metadataFilename, e);
        		throw e;
        	}

        }
        String name= null;
        String url= null;
        String id= null;
        Configuration config= null;
        // SLCS SP
        config= metadata.subset("ServiceProvider");
        if (!config.isEmpty()) {
            LOG.debug("ServiceProvider element found");
            id= config.getString("[@id]");
            if (id == null || id.equals("")) {
                id= DEFAULT_SLCS_PROVIDERID;
            }
            this.slcsProviderId_= id;
            name= config.getString("name");
            url= config.getString("url");
            ServiceProvider sp= new ServiceProvider(id, name, url);
            LOG.debug("add " + sp);
            entities.put(id, sp);
        }
        else {
            throw new SLCSConfigurationException("ServiceProvider element not found in metadata");
        }
        // All IdPs
        Configuration idpsConfig= metadata.subset("IdentityProviders");
        if (idpsConfig.isEmpty()) {
            throw new SLCSConfigurationException("IdentityProviders element not found in metadata");
        }
        List idps= idpsConfig.getList("IdentityProvider[@id]");
        int nIdp= idps.size();
        if (nIdp < 1) {
            throw new SLCSConfigurationException("No IdentityProvider element found in metadata");
        }
        LOG.debug(nIdp + " IdentityProvider elements found");
        for (int i= 0; i < nIdp; i++) {
            config= idpsConfig.subset("IdentityProvider(" + i + ")");
            id= config.getString("[@id]");
            name= config.getString("name");
            url= config.getString("url");
            String authTypeName= config.getString("authentication[@type]");
            String authUrl= config.getString("authentication.url");
            if (authUrl == null) {
                authUrl= url;
            }
            IdentityProvider idp= new IdentityProvider(id,
                                                       name,
                                                       url,
                                                       authTypeName,
                                                       authUrl);
            if (idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_CAS
                    || idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_PUBCOOKIE) {
                // read form name and username and password field names
                String formName= config.getString("authentication.form[@name]");
                idp.setAuthFormName(formName);
                String formUsername= config.getString("authentication.form.username");
                idp.setAuthFormUsername(formUsername);
                String formPassword= config.getString("authentication.form.password");
                idp.setAuthFormPassword(formPassword);
            }
            else {
                // basic or ntlm
                String realm= config.getString("authentication.realm");
                idp.setAuthRealm(realm);
            }
            LOG.debug("add " + idp);
            entities.put(id, idp);
        }
        return entities;
    }

    /**
     * 
     * @param providerId
     * @return
     */
    public ServiceProvider getServiceProvider(String providerId) {
        ServiceProvider sp= (ServiceProvider) this.providers_.get(providerId);
        return sp;
    }

    /**
     * 
     * @return
     */
    public ServiceProvider getSLCS() {
        ServiceProvider sp= (ServiceProvider) this.providers_.get(this.slcsProviderId_);
        return sp;
    }

    /**
     * 
     * @param providerId
     * @return
     */
    public IdentityProvider getIdentityProvider(String providerId) {
        IdentityProvider idp= (IdentityProvider) this.providers_.get(providerId);
        return idp;
    }

    /**
     * 
     * @param providerId
     * @return The <code>Provider</code> identified by this providerId or
     *         <code>null</code> if the provider doesn't exist.
     * @see org.glite.slcs.shibclient.metadata.Provider
     */
    public Provider getProvider(String providerId) {
        return (Provider) providers_.get(providerId);
    }

    /**
     * @return An iterator of all <code>Provider</code> objects
     */
    public Iterator getProviders() {
        Collection providers= providers_.values();
        return providers.iterator();
    }

    /**
     * @return An iterator of all providerIds (<code>String</code>)
     */
    public Iterator getProviderIds() {
        Set providerIds= providers_.keySet();
        return providerIds.iterator();
    }

    /**
     * @return An enumeration of all <code>IdentityProvider</code>
     */
    public Enumeration getIdentityProviders() {
        Vector idps= new Vector();
        Iterator providers= getProviders();
        while (providers.hasNext()) {
            Provider provider= (Provider) providers.next();
            if (provider instanceof IdentityProvider) {
                idps.add(provider);
            }
        }
        return idps.elements();
    }

    /**
     * TEST DRIVE
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // slcs-client.xml config in classpath
        ShibbolethClientMetadata metadata= new ShibbolethClientMetadata("slcs-client.xml");
        Iterator entities= metadata.getProviders();
        while (entities.hasNext()) {
            Provider entity= (Provider) entities.next();
            System.out.println(entity);
        }
    }

}
