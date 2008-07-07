/*
 * $Id: SLCSClientVersion.java,v 1.18 2008/07/07 09:18:23 vtschopp Exp $
 * 
 * Created on Aug 9, 2006 by tschopp
 *
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */
package org.glite.slcs;

public class SLCSClientVersion {

    /** Major version number */
    static public final int MAJOR= 1;
    /** Minor version number */
    static public final int MINOR= 3;
    /** Revision version number */
    static public final int REVISION= 3;
     /** Build number */
    static public final int BUILD= 1;
    
    /** Copyright */
    static public final String COPYRIGHT= "Copyright (c) 2008. Members of the EGEE Collaboration";

    /**
     * Prevents instantiation
     */
    private SLCSClientVersion() {}

    /**
     * @return The version number in format MAJOR.MINOR.REVISION-BUILD
     */
    static public String getVersion() {
        StringBuffer sb= new StringBuffer();
        sb.append(MAJOR).append('.');
        sb.append(MINOR).append('.');
        sb.append(REVISION).append('-');
        sb.append(BUILD);
        return sb.toString();
    }

}
