/**
 * 
 */
package com.thinkbiganalytics.metadata.upgrade;

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

import javax.inject.Inject;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.app.KyloVersion;

/**
 * Performs all upgrade steps within a metadata transaction.
 */
public class KyloUpgrader {
    
    
    
    @Inject
    private MetadataAccess metadata;
    
    @Inject
    private UpgradeKyloService upgradeService;
    

    public boolean upgrade() {
        return metadata.commit(() -> {
            KyloVersion version = upgradeService.getCurrentVersion();
            
            return upgradeService.upgradeFrom(version);
        }, MetadataAccess.SERVICE);
    }

}
