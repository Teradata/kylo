package com.thinkbiganalytics;

/*-
 * #%L
 * kylo-commons-util
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
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
 * #L%
 */

/**
 * Represents the current Kylo Version deployed in the system
 */
public interface KyloVersion extends Comparable<KyloVersion> {

    /**
     * Return the full Kylo Version string as {@link #getMajorVersion()}.{@link #getMinorVersion()}
     *
     * @return the full kylo version string
     */
    String getVersion();

    /**
     * Return the {@link #getMajorVersion()} as a numeric value
     *
     * @return the numeric value of the major version
     */
    Float getMajorVersionNumber();
    
    /**
     * Return the major versoin of Kylo
     *
     * @return the major version string
     */
    String getMajorVersion();

    /**
     * Return the minor version of Kylo
     *
     * @return the minor version of Kylo
     */
    String getMinorVersion();
    
    /**
     * @return the point version of Kylo
     */
    String getPointVersion();

    /**
     * Return any tag that is part of the version; i.e any string following a "-" after 
     * the version numbers.  For example, if the version is 0.8.1-SNAPSHOT then the tag
     * returned would be "SNAPSHOT".  Returns an empty string if there is no tag.
     * 
     * @return the tag or and empty string if this version has no tag
     */
    String getTag();
    
    /**
     * @return this version without the tag value.
     */
    KyloVersion withoutTag();

    /**
     * Return a description of the Kylo version deployed
     *
     * @return a description of the Kylo version deployed
     */
    String getDescription();
    
    boolean matches(String major, String minor, String point);
    
    boolean matches(String major, String minor, String point, String tag);

    /**
     * Update the current Kylo version to the passed in version
     *
     * @param v the version to update to
     * @return the updated version
     */
    KyloVersion update(KyloVersion v);
}
