/*
 * $Id: SLCSInfo.java,v 1.4 2008/04/25 11:46:53 vtschopp Exp $
 * 
 * Created on Oct 20, 2006 by Valery Tschopp <tschopp@switch.ch>
 *
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */
package org.glite.slcs;

import java.io.File;
import java.util.Enumeration;

import org.glite.slcs.config.SLCSClientConfiguration;
import org.glite.slcs.shibclient.metadata.IdentityProvider;
import org.glite.slcs.shibclient.metadata.ServiceProvider;
import org.glite.slcs.shibclient.metadata.ShibbolethClientMetadata;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SLCSInfo {

    /** Logging */
    private static Log LOG= LogFactory.getLog(SLCSInfo.class);

    /** Default XML config filename in CLASSPATH */
    static private String DEFAULT_CONFIGURATION_FILE= "slcs-init.xml";

    /**
     * @param args
     */
    public static void main(String[] args) {
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
                    + SLCSClientVersion.COPYRIGHT);
            System.out.println("Version: " + SLCSClientVersion.getVersion());
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
        if (verbose) {
            System.out.println("Config: " + config);
        }

        // read SLCS config
        SLCSClientConfiguration configuration= null;
        // and parse metadata        
        ShibbolethClientMetadata metadata= null;
        try {
            LOG.debug("load SLCS client configuration...");
            configuration= SLCSClientConfiguration.getInstance(config);
            metadata= new ShibbolethClientMetadata(configuration);
        } catch (SLCSConfigurationException e) {
            LOG.fatal("SLCS info error", e);
            System.err.println("ERROR: SLCS info: " + e);
            System.exit(1);
        }

        ServiceProvider slcs= metadata.getSLCS();
        System.out.println("SLCS Service URL: " + slcs.getUrl());
        Enumeration<IdentityProvider> idps= metadata.getIdentityProviders();
        while (idps.hasMoreElements()) {
            IdentityProvider idp= (IdentityProvider) idps.nextElement();
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
