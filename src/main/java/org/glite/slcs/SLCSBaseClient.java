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
package org.glite.slcs;

import java.io.File;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.glite.slcs.config.SLCSClientConfiguration;
import org.glite.slcs.httpclient.ssl.ExtendedProtocolSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for slcs-init and slcs-info
 * 
 * @author Valery Tschopp <tschopp@switch.ch>
 */
public abstract class SLCSBaseClient {

    /** Logging */
    private static Logger LOG= LoggerFactory.getLogger(SLCSBaseClient.class);

    /** Default XML config filename in CLASSPATH */
    static protected String DEFAULT_CONFIGURATION_FILE= "slcs-init.xml";

    static protected void registerSSLTrustStore(SLCSClientConfiguration configuration) throws SLCSException {
        String truststorePath= getDefaultHttpClientTrustStoreFile(configuration);
        try {
            ExtendedProtocolSocketFactory epsf = new ExtendedProtocolSocketFactory(truststorePath);
            Protocol https = new Protocol("https", (ProtocolSocketFactory)epsf, 443);
            Protocol.registerProtocol("https", https);
            // BUG FIX: register the truststore as default SSLSocketFactory
            // to download the metadata from https (QuoVadis CAs not in all default cacerts)
            LOG.info("register ExtendedProtocolSocketFactory(" + truststorePath + ") as default SSL socket factory");
            SSLContext sc = epsf.getSSLContext();
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new SLCSException(
                    "Failed to create ExtendedProtocolSocketFactory", e);
        }

    }
    
    
    static protected String getDefaultHttpClientTrustStoreFile(SLCSClientConfiguration configuration)
        throws SLCSConfigurationException {
        String truststoreFile = configuration.getString("HttpClient.TrustStoreFile");
        LOG.info("TrustStoreFile=" + truststoreFile);
        return truststoreFile;
    }


    /**
     * @param configuration
     *            SLCS client configuration.
     * @return
     * @throws SLCSConfigurationException
     */
    protected static String getDefaultStoreDirectory(SLCSClientConfiguration configuration)
            throws SLCSConfigurationException {
                String storeDirectory = configuration.getString("StoreDirectory");
                LOG.debug("StoreDirectory=" + storeDirectory);
                // check java properties variable and expand
                int start = storeDirectory.indexOf("${");
                if (0 <= start) {
                    int stop = storeDirectory.indexOf("}");
                    if (start <= stop) {
                        String propertyName = storeDirectory.substring(start + 2, stop);
                        String propertyValue = System.getProperty(propertyName);
                        if (propertyValue != null) {
                            LOG.debug("replace ${" + propertyName + "} with: "
                                    + propertyValue);
                            // Windows uses backslash, must be escaped !!!
                            propertyValue = StringUtils.replace(propertyValue, "\\",
                                                                "/", -1);
                            String replace = "${" + propertyName + "}";
                            storeDirectory = StringUtils.replace(storeDirectory,
                                                                 replace,
                                                                 propertyValue, -1);
                        }
                        else {
                            LOG.error("StoreDirectory contains invalid ${"
                                    + propertyName + "} java property");
                            throw new SLCSConfigurationException(
                                    "StoreDirectory contains invalid ${" + propertyName
                                            + "} java property");
                        }
                    }
                    else {
                        // ERROR
                        LOG.error("StoreDirectory contains invalid ${...} java property");
                        throw new SLCSConfigurationException(
                                "StoreDirectory contains invalid ${...} java property");
                    }
                }
                // get absolute pathname
                File directory = new File(storeDirectory);
                storeDirectory = directory.getAbsolutePath();
                LOG.info("StoreDirectory=" + storeDirectory);
                return storeDirectory;
            }


    /**
     * Returns the default UserCertFile from the config.
     * 
     * @param configuration
     *            the XML config obj.
     * @return The usercert filename.
     * @throws SLCSConfigurationException
     */
    protected static String getDefaultUserCertFile(SLCSClientConfiguration configuration)
            throws SLCSConfigurationException {
                String userCertFile = configuration.getString("UserCertFile");
                LOG.info("UserCertFile=" + userCertFile);
                return userCertFile;
            }


    /**
     * Returns the default UserKeyFile from the config.
     * 
     * @param configuration
     *            the XML config obj.
     * @return The userkey filename.
     * @throws SLCSConfigurationException
     */
    protected static String getDefaultUserKeyFile(SLCSClientConfiguration configuration)
            throws SLCSConfigurationException {
                String userKeyFile = configuration.getString("UserKeyFile");
                LOG.info("UserKeyFile=" + userKeyFile);
                return userKeyFile;
            }


    protected static String getDefaultUserPKCS12File(SLCSClientConfiguration configuration)
            throws SLCSConfigurationException {
                String userPKCS12File = configuration.getString("UserPKCS12File");
                LOG.info("UserPKCS12File=" + userPKCS12File);
                return userPKCS12File;
            }


    /**
     * Returns the default private KeySize from the config.
     * 
     * @param configuration
     *            the XML config obj.
     * @return the key size (bits).
     * @throws SLCSConfigurationException
     */
    protected static int getDefaultUserKeySize(SLCSClientConfiguration configuration)
            throws SLCSConfigurationException {
                int keySize = configuration.getInt("UserKeySize");
                // check valid key size
                if (!validKeySize(keySize)) {
                    LOG.error("Invalid UserKeySize: " + keySize);
                    throw new SLCSConfigurationException("Invalid UserKeySize: "
                            + keySize);
                }
                LOG.info("KeySize=" + keySize);
                return keySize;
            }


    /**
     * Checks that the size is 1024 or 2048
     * 
     * @param size
     *            the key size
     * @return <code>true</code> if the size valid.
     */
    protected static boolean validKeySize(int size) {
        boolean valid = false;
        if (size == 1024 || size == 2048) {
            valid = true;
        }
        return valid;
    }

}
