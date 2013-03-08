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
package org.glite.slcs.ui;

/**
 * Version class to retrieve version info from jar manifest.
 * @since 1.4.0
 */
public class Version {

    static final String COPYRIGHT= "Copyright (c) 2010 SWITCH";
    static final Package PKG= Version.class.getPackage();

    /**
     * @return the copyright string
     */
    static public String getCopyright() {
        return COPYRIGHT;
    }
    
    /**
     * Returns the implementation version from the jar MANIFEST.
     * @return the implementation version
     */
    static public String getVersion() {
        return PKG.getImplementationVersion();   
    }

    /**
     * Returns the implementation title from the jar MANIFEST.
     * 
     * @return the implementation title
     */
    static public String getName() {
        return PKG.getImplementationTitle();   
    }

    /**
     * Constructor. Prevents instantiation.
     */
    private Version() {
    }
}
