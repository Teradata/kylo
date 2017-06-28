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

import com.thinkbiganalytics.KyloVersion;

/**
 * Provider to return/update metadata representing the current Kylo version deployed
 */
public interface KyloVersionProvider {
    
    /**
     * @return true if the current version is equal to the latest version
     */
    boolean isUpToDate();

    /**
     * Return the current Kylo version,
     *
     * @return the current kylo version deployed
     */
    KyloVersion getCurrentVersion();
    
    /**
     * Sets a new Kylo current version.
     * @param version the new version
     */
    void setCurrentVersion(KyloVersion version);
    
    /**
     * @return the version of the deployed Kylo build
     */
    KyloVersion getBuildVersion();
}
