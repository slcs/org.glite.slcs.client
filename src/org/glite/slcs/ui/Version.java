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
 * $Id: Version.java,v 1.2 2009/08/19 14:40:24 vtschopp Exp $
 */
package org.glite.slcs.ui;

/**
 * Version class to retrieve version info from jar manifest.
 */
public class Version {

    static final String COPYRIGHT= "Copyright (c) 2008-2009. Members of the EGEE Collaboration";
    static final Package PKG= Version.class.getPackage();

    /**
     * @return the copyright string
     */
    static public String getCopyright() {
        return COPYRIGHT;
    }
    
    /**
     * @return the implementation version from MANIFEST
     */
    static public String getVersion() {
        return PKG.getImplementationVersion();   
    }

    /**
     * @return the implementation title from MANIFEST
     */
    static public String getName() {
        return PKG.getImplementationTitle();   
    }

}
