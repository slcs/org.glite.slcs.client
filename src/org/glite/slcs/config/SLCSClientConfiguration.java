/*
 * $Id: SLCSClientConfiguration.java,v 1.1 2006/10/24 09:24:18 vtschopp Exp $
 * 
 * Created on Aug 9, 2006 by tschopp
 *
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */
package org.glite.slcs.config;

import org.glite.slcs.SLCSConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SLCSClientConfiguration is the XML file based configuration for the SLCS
 * client.
 * 
 * @author Valery Tschopp <tschopp@switch.ch>
 * @version $Revision: 1.1 $
 */
public class SLCSClientConfiguration extends SLCSConfiguration {

    /** Logging */
    private static Log LOG= LogFactory.getLog(SLCSClientConfiguration.class);

    /** Singleton pattern */
    private static SLCSClientConfiguration SINGLETON= null;

    /**
     * Factory pattern
     * 
     * @param filename
     *            The XML configuration filename
     * @return
     * @throws SLCSConfigurationException
     *             iff the configuration is not valid
     */
    public static synchronized SLCSClientConfiguration getInstance(
            String filename) throws SLCSConfigurationException {
        if (SINGLETON == null) {
            SINGLETON= new SLCSClientConfiguration(filename);
        }
        return SINGLETON;
    }

    /**
     * Constructor
     * 
     * @param filename
     *            The XML config filename
     * @throws SLCSConfigurationException
     *             if an configuration error occurs
     */
    private SLCSClientConfiguration(String filename)
            throws SLCSConfigurationException {
        super(filename);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.glite.slcs.config.SLCSConfiguration#checkConfiguration()
     */
    protected void checkConfiguration() throws SLCSConfigurationException {
        // HttpClient.TrustStoreFile
        if (!contains("HttpClient.TrustStoreFile")) {
            String filename= getFilename();
            LOG.error("SLCSClientConfiguration(" + filename
                    + "): HttpClient.TrustStoreFile element missing");
            throw new SLCSConfigurationException("Element HttpClient.TrustStoreFile not defined in: "
                    + filename);
        }
        // StoreDirectory
        if (!contains("StoreDirectory")) {
            String filename= getFilename();
            LOG.error("SLCSClientConfiguration(" + filename
                    + "): StoreDirectory element missing");
            throw new SLCSConfigurationException("Element StoreDirectory not defined in: "
                    + filename);
        }
        // StoreDirectory
        if (!contains("UserKeyFile")) {
            String filename= getFilename();
            LOG.error("SLCSClientConfiguration(" + filename
                    + "): UserKeyFile element missing");
            throw new SLCSConfigurationException("Element UserKeyFile not defined in: "
                    + filename);
        }
        // StoreDirectory
        if (!contains("UserCertFile")) {
            String filename= getFilename();
            LOG.error("SLCSClientConfiguration(" + filename
                    + "): UserCertFile element missing");
            throw new SLCSConfigurationException("Element UserCertFile not defined in: "
                    + filename);
        }
        // StoreDirectory
        if (!contains("UserKeySize")) {
            String filename= getFilename();
            LOG.error("SLCSClientConfiguration(" + filename
                    + "): UserKeySize element missing");
            throw new SLCSConfigurationException("Element UserKeySize not defined in: "
                    + filename);
        }

    }

}
