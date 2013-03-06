/*
 * $Id: ServiceProvider.java,v 1.1 2006/10/24 09:24:19 vtschopp Exp $
 * 
 * Created on Jul 6, 2006 by tschopp
 *
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */
package org.glite.slcs.shibclient.metadata;

/**
 * ServiceProvider is a Shibboleth SP description
 *
 * @author Valery Tschopp <tschopp@switch.ch>
 * @version $Revision: 1.1 $
 */
public class ServiceProvider extends Provider {

    public ServiceProvider(String id, String name, String url) {
        super(id, name, url);
    }

}
