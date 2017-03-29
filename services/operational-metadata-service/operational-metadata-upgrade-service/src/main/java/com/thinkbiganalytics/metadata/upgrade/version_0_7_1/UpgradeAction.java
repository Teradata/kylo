/**
 * 
 */
package com.thinkbiganalytics.metadata.upgrade.version_0_7_1;

/*-
 * #%L
 * kylo-operational-metadata-upgrade-service
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.metadata.api.app.KyloVersion;
import com.thinkbiganalytics.metadata.upgrade.UpgradeState;

/**
 *
 */
public class UpgradeAction implements UpgradeState {
    
    private static final Logger log = LoggerFactory.getLogger(UpgradeAction.class);

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.upgrade.UpgradeState#getStartingVersion()
     */
    @Override
    public KyloVersion getStartingVersion() {
        return asVersion("0.7", "1");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.upgrade.UpgradeState#upgradeFrom(com.thinkbiganalytics.metadata.api.app.KyloVersion)
     */
    @Override
    public void upgradeFrom(KyloVersion startingVersion) {
        if (getStartingVersion().equals(startingVersion)) {
            log.info("Upgrading from version: " + startingVersion);
            log.info("Upgrad complete");
        }
    }

}
