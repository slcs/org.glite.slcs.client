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
 * $Id: SLCSClientVersion.java,v 1.28 2009/08/19 14:41:47 vtschopp Exp $
 */
package org.glite.slcs;

import org.glite.slcs.ui.Version;

public class SLCSClientVersion {

    /** 
     * Copyright
     * @deprecated use {@link Version#getCopyright()}
     */
    static public final String COPYRIGHT= "Copyright (c) 2008-2009. Members of the EGEE Collaboration";

    /**
     * Prevents instantiation
     */
    private SLCSClientVersion() {}

    /**
     * @return The version number in format MAJOR.MINOR.REVISION-BUILD
     * @deprecated use {@link Version#getVersion()}
     */
    static public String getVersion() {
        return Version.getVersion();
    }

}
