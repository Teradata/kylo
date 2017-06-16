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
import com.thinkbiganalytics.KyloVersionUtil;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.api.app.KyloVersionProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

@Order(PostMetadataConfigAction.LATE_ORDER + 100)
public class KyloUpgradeService implements PostMetadataConfigAction {

    private static final Logger log = LoggerFactory.getLogger(KyloUpgradeService.class);

    public static final List<KyloVersion> UPGRADE_SEQUENCE;
    static {
        String[] versions = {
              "0.4.0",
              "0.4.1",
              "0.4.2",
              "0.4.3",
              "0.5.0",
              "0.6.0",
              "0.6.1",
              "0.6.2",
              "0.6.3",
              "0.6.4",
              "0.7.0",
              "0.8.0",
              "0.8.0.1",
              "0.8.1"
        };
        
        UPGRADE_SEQUENCE = Collections.unmodifiableList(Arrays.stream(versions)
                                                            .map(KyloVersionUtil::parseVersion)
                                                            .sorted()
                                                            .collect(Collectors.toList()));
    }

    @Inject
    private MetadataAccess metadataAccess;
    @Inject
    private KyloVersionProvider versionProvider;
    @Inject
    private Optional<List<UpgradeState>> upgradeActions;
    
    private final KyloVersion buildVersion = KyloVersionUtil.getBuildVersion();
    
    
    /**
     * Upgrades to the next Kylo version relative to the current version.
     * @return true if after this call upgrades are complete and Kylo is up-to-date.
     */
    public boolean upgradeNext() {
        boolean isComplete = false;
        
        if (this.upgradeActions.isPresent()) {
            KyloVersion nextVersion = metadataAccess.read(() -> {
                return getNextVersion();
            }, MetadataAccess.SERVICE);
            
            isComplete = metadataAccess.commit(() -> {
                this.upgradeActions.get().stream()
                    .filter(a -> a.isTargetVersion(nextVersion))
                    .forEach(a -> a.upgradeTo(nextVersion));
                versionProvider.setCurrentVersion(nextVersion);
                return nextVersion.equals(this.buildVersion);
            }, MetadataAccess.SERVICE);
            
            log.info("=================================");
            log.info("Finished upgrade through v{}", nextVersion);
            
            return isComplete;
        } else {
            isComplete = metadataAccess.commit(() -> {
                versionProvider.setCurrentVersion(this.buildVersion);
                return true;
            }, MetadataAccess.SERVICE);
            
            log.info("=================================");
            log.info("Finished upgrade through v{}", this.buildVersion);
        }
        
        return isComplete;
    }
    
    /**
     * This service as a PostMetadataConfigAction will invoke fresh install actions
     * after the metadata store is started and configured.
     */
    @Override
    public void run() {
        if (this.upgradeActions.isPresent()) {
            metadataAccess.commit(() -> {
                this.upgradeActions.get().stream()
                    .filter(a -> a.isTargetFreshInstall())
                    .forEach(a -> a.upgradeTo(this.buildVersion));
                
                log.info("=================================");
                log.info("Finished fresh install updates");
            }, MetadataAccess.SERVICE);
        }
    }
    

    private KyloVersion getNextVersion() {
        KyloVersion current = versionProvider.getCurrentVersion();
        // If there are no recorded versions then this is a fresh install so the next version is the actual build version.
        if (current == null) {
            return this.buildVersion;
        } else {
            int idx = UPGRADE_SEQUENCE.indexOf(current);
            return idx == UPGRADE_SEQUENCE.size() - 1 ? null : UPGRADE_SEQUENCE.get(idx + 1);
        }
    }
}
