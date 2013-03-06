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
 * Version: $Id: SLCSClientConfiguration.java,v 1.3 2009/08/19 14:46:30 vtschopp Exp $
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
 * @version $Revision: 1.3 $
 */
public class SLCSClientConfiguration extends SLCSConfiguration {

	/** Logging */
	private static Log LOG = LogFactory.getLog(SLCSClientConfiguration.class);

	/** Singleton pattern */
	private static SLCSClientConfiguration SINGLETON = null;

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
			SINGLETON = new SLCSClientConfiguration(filename);
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
			String filename = getFilename();
			LOG.error("SLCSClientConfiguration(" + filename
					+ "): HttpClient.TrustStoreFile element missing");
			throw new SLCSConfigurationException(
					"Element HttpClient.TrustStoreFile not defined in: "
							+ filename);
		}
		// StoreDirectory
		if (!contains("StoreDirectory")) {
			String filename = getFilename();
			LOG.error("SLCSClientConfiguration(" + filename
					+ "): StoreDirectory element missing");
			throw new SLCSConfigurationException(
					"Element StoreDirectory not defined in: " + filename);
		}
		// UserKeyFile
		if (!contains("UserKeyFile")) {
			String filename = getFilename();
			LOG.error("SLCSClientConfiguration(" + filename
					+ "): UserKeyFile element missing");
			throw new SLCSConfigurationException(
					"Element UserKeyFile not defined in: " + filename);
		}
		// UserCertFile
		if (!contains("UserCertFile")) {
			String filename = getFilename();
			LOG.error("SLCSClientConfiguration(" + filename
					+ "): UserCertFile element missing");
			throw new SLCSConfigurationException(
					"Element UserCertFile not defined in: " + filename);
		}
		// UserKeySize
		if (!contains("UserKeySize")) {
			String filename = getFilename();
			LOG.error("SLCSClientConfiguration(" + filename
					+ "): UserKeySize element missing");
			throw new SLCSConfigurationException(
					"Element UserKeySize not defined in: " + filename);
		}
		// ShibbolethClientMetadata
		if (getConfiguration().subset("ShibbolethClientMetadata").isEmpty()) {
			String filename = getFilename();
			LOG.error("SLCSClientConfiguration(" + filename
					+ "): ShibbolethClientMetadata element missing");
			throw new SLCSConfigurationException(
					"Element ShibbolethClientMetadata not defined in: "
							+ filename);
		}

	}

    /**
     * @return The absolute filename or URL used as source for the SLCS config.
     */
    public String getConfigSource() {
        return getFilename();
    }

}
