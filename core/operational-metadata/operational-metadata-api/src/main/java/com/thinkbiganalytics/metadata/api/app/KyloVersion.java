package com.thinkbiganalytics.metadata.api.app;

/*-
 * #%L
 * thinkbig-operational-metadata-api
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
public interface KyloVersion {

    /**
     * Return the full Kylo Version string as {@link this#getMajorVersion()}.{@link this#getMinorVersion()}
     *
     * @return the full kylo version string
     */
    String getVersion();

    /**
     * Return the {@link this#getMajorVersion()} as a numeric value
     *
     * @return the numeric value of the major version
     */
    Float getMajorVersionNumber();

    /**
     * Return the minor version of Kylo
     *
     * @return the minor version of Kylo
     */
    String getMinorVersion();

    /**
     * Return the major versoin of Kylo
     *
     * @return the major version string
     */
    String getMajorVersion();


    /**
     * Return a description of the Kylo version deployed
     *
     * @return a description of the Kylo version deployed
     */
    String getDescription();

    /**
     * Update the current Kylo version to the passed in version
     *
     * @param v the version to update to
     * @return the updated version
     */
    KyloVersion update(KyloVersion v);
}
