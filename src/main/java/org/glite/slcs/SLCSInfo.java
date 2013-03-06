/*
 * Copyright (c) 2007-2009. Members of the EGEE Collaboration.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id: SLCSInfo.java,v 1.10 2010/02/09 17:06:48 vtschopp Exp $
 */
package org.glite.slcs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glite.slcs.config.SLCSClientConfiguration;
import org.glite.slcs.shibclient.metadata.IdentityProvider;
import org.glite.slcs.shibclient.metadata.ServiceProvider;
import org.glite.slcs.shibclient.metadata.ShibbolethClientMetadata;
import org.glite.slcs.ui.Version;

public class SLCSInfo extends SLCSBaseClient {

    /** Logging */
    private static Log LOG= LogFactory.getLog(SLCSInfo.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        LOG.info("Version: ui " + Version.getVersion() + ", common " + org.glite.slcs.common.Version.getVersion());

        CommandLineParser parser= new PosixParser();
        CommandLine cmd= null;
        boolean error= false;
        Options options= createCommandLineOptions();
        try {
            cmd= parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("ERROR: " + e.getMessage());
            error= true;
        }

        // help?
        if (error || cmd.hasOption('h')) {
            HelpFormatter help= new HelpFormatter();
            help.printHelp("slcs-info [options]", options);
            System.exit(1);
        }

        // version?
        if (cmd.hasOption('V')) {
            System.out.println("slcs-info: " + SLCSInfo.class.getName() + " - "
                    + Version.getCopyright());
            System.out.println("Version: ui " + Version.getVersion() + ", common " + org.glite.slcs.common.Version.getVersion());
            System.exit(0);
        }

        // verbose?
        boolean verbose= false;
        if (cmd.hasOption('v')) {
            verbose= true;
        }

        // config
        String config= null;
        if (cmd.hasOption('c')) {
            config= cmd.getOptionValue('c');
            if (config == null) {
                System.err.println("ERROR: --config: empty config filename");
                System.exit(1);
            }
            File configFile= new File(config);
            if (!configFile.exists()) {
                System.err.println("ERROR: config file: " + config
                        + " doesn't exist");
                System.exit(1);
            }
        }
        else {
            config= DEFAULT_CONFIGURATION_FILE;
        }

        // read SLCS config
        SLCSClientConfiguration configuration= null;
        // and parse metadata        
        ShibbolethClientMetadata metadata= null;
        try {
            LOG.debug("load SLCS client configuration...");
            configuration= SLCSClientConfiguration.getInstance(config);
            // install truststore for QuoVadis certs
            registerSSLTrustStore(configuration);
            metadata= new ShibbolethClientMetadata(configuration);
            if (verbose) {
                System.out.println("Config: " + configuration.getConfigSource());
                System.out.println("Metadata: " + metadata.getMetadataSource() );
            }
        } catch (SLCSException e) {
            LOG.fatal("SLCS info error", e);
            System.err.println("ERROR: SLCS info: " + e);
            System.exit(1);
        }

        ServiceProvider slcs= metadata.getSLCS();
        System.out.println("SLCS Service URL: " + slcs.getUrl());
        // sort by providerId and display
        Enumeration<IdentityProvider> idps= metadata.getIdentityProviders();
        List<IdentityProvider> sortedIdps= new ArrayList<IdentityProvider>();
        while (idps.hasMoreElements()) {
            sortedIdps.add(idps.nextElement());
        }
        Collections.sort(sortedIdps);
        for (IdentityProvider idp : sortedIdps) {            
            System.out.println("Identity ProviderID: " + idp.getId() + " [" + idp.getName() + "]");
        }
        
    }


    /**
     * Creates the CLI options.
     * 
     * @return The CLI Options
     */
    private static Options createCommandLineOptions() {
        Option help= new Option("h", "help", false, "this help");
        Option config= new Option("c",
                                  "conf",
                                  true,
                                  "SLCS client XML configuration file");
        config.setArgName("filename");
        Option verbose= new Option("v", "verbose", false, "verbose");
        Option version= new Option("V", "version", false, "shows the version");
//        Option list= new Option("l", "list", false, "list the IdP providerIds");
        Options options= new Options();
        options.addOption(help);
//        options.addOption(list);
        options.addOption(config);
        options.addOption(verbose);
        options.addOption(version);
        return options;
    }

}
