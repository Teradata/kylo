/**
 * 
 */
package com.thinkbiganalytics.server.upgrade;

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

import com.thinkbiganalytics.KyloVersion;

import org.springframework.core.annotation.Order;

/**
 * Implemented by beans that should be invoked during the Kylo upgrade process.
 */
@Order(UpgradeAction.DEFAULT_ORDER)
public interface UpgradeAction { 
    
    int EARLY_ORDER = -10000;
    int DEFAULT_ORDER = 0;
    int LATE_ORDER = 10000;
    
    /**
     * Indicates whether this action should be invoked during a fresh install before 
     * version migration begins.
     * @param currentVersion the starting version in the upgrade sequence
     * @return true if this action will participate in the fresh install before migration
     */
    default boolean isTargetPreFreshInstall(KyloVersion currentVersion) {
        return false;
    }
    
    /**
     * Indicates whether this action should be invoked during the migration
     * sequence for the specified version.
     * @param version the current version in the migration sequence
     * @return true if this action will participate in the upgrade of to this version
     */
    default boolean isTargetVersion(KyloVersion version) {
        return false;
    }
    
    /**
     * Indicates whether this action should be invoked during a fresh install after 
     * the final version migration has completed.
     * @param initialVersion the final version in the upgrade sequence
     * @return true if this action will participate in the fresh install after migration
     */
    default boolean isTargetFreshInstall(KyloVersion finalVersion) {
        return false;
    }

    /**
     * Invoke if when one of the isTarget* methods returns true during the upgrade procedure.
     * @param version the current target version
     */
    void upgradeTo(KyloVersion version);

}
