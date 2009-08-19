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
 * $Id: Version.java,v 1.1 2009/08/19 14:13:56 vtschopp Exp $
 */
package org.glite.slcs.ui;

/**
 * Version class to retrieve version info from jar manifest.
 */
public class Version {

    /**
     * @return the implementation version from MANIFEST
     */
    static public String getVersion() {
        Package pkg= Version.class.getPackage();
        return pkg.getImplementationVersion();   
    }

    /**
     * @return the implementation title from MANIFEST
     */
    static public String getName() {
        Package pkg= Version.class.getPackage();
        return pkg.getImplementationTitle();   
    }

}
