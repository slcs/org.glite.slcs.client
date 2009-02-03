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
 * $Id: SLCSClientVersion.java,v 1.22 2009/02/03 09:45:16 vtschopp Exp $
 */
package org.glite.slcs;

public class SLCSClientVersion {

    /** Major version number */
    static public final int MAJOR= 1;
    /** Minor version number */
    static public final int MINOR= 3;
    /** Revision version number */
    static public final int REVISION= 4;
     /** Build number */
    static public final int BUILD= 2;
    
    /** Copyright */
    static public final String COPYRIGHT= "Copyright (c) 2008-2009. Members of the EGEE Collaboration";

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
