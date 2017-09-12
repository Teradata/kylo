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
import com.thinkbiganalytics.metadata.api.app.KyloVersionProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class KyloUpgradeService {

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
              "0.8.1",
              "0.8.2",
              "0.8.3",
              "0.8.3.1"
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
    
    private KyloVersion buildVersion;
    private boolean freshInstall = false;
    
    
    /**
     * Upgrades to the next Kylo version relative to the current version.
     * @return true if after this call upgrades are complete and Kylo is up-to-date.
     */
    public boolean upgradeNext() {
        boolean isComplete = false;
        
        if (this.upgradeActions.isPresent()) {
            // Determine the next version from the current version.  If this is
            // a fresh install then the build version will be returned.  If
            // there are no versions later than the current one (we are up-to-date)
            // then nextVerion will be null.
            KyloVersion nextVersion = metadataAccess.read(() -> {
                return getNextVersion();
            }, MetadataAccess.SERVICE);
            
            if (nextVersion != null) {
                // Invoke any upgrade actions for this next version and update it to be the current version.
                isComplete = metadataAccess.commit(() -> {
                    this.upgradeActions.get().stream()
                        .filter(a -> a.isTargetVersion(nextVersion))
                        .forEach(a -> a.upgradeTo(nextVersion));
                    versionProvider.setCurrentVersion(nextVersion);
                    
                    log.info("=================================");
                    log.info("Finished upgrade through v{}", nextVersion);
                    
                    return nextVersion.equals(this.buildVersion);
                }, MetadataAccess.SERVICE);
                
                // If upgrades are complete and this was starting from a fresh install then invoke any
                // fresh install actions.
                if (isComplete && this.freshInstall) {
                    metadataAccess.commit(() -> {
                        this.upgradeActions.get().stream()
                            .filter(a -> a.isTargetFreshInstall())
                            .forEach(a -> a.upgradeTo(this.buildVersion));
                        
                        log.info("=================================");
                        log.info("Finished fresh install updates");
                    }, MetadataAccess.SERVICE);
                }
                
                return isComplete;
            } else {
                log.info("Nothing left to upgrade - Kylo is up-to-date");
                return true;
            }
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
    
    @PostConstruct
    public void init() {
        KyloVersion current = versionProvider.getCurrentVersion();
        this.freshInstall = current == null;
        this.buildVersion = KyloVersionUtil.getBuildVersion();
    }

    private KyloVersion getNextVersion() {
        KyloVersion current = versionProvider.getCurrentVersion();
        // If there are no recorded versions then this is a fresh install so the next version is the actual build version.
        if (current == null) {
            return this.buildVersion;
        } else {
            int idx = IntStream.range(0, UPGRADE_SEQUENCE.size())
                .filter(i -> 
                        UPGRADE_SEQUENCE.get(i).matches(current.getMajorVersion(), current.getMinorVersion(), current.getPointVersion()) ||
                        UPGRADE_SEQUENCE.get(i).compareTo(current) > 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("The current Kylo version is unrecognized: " + current));
            // If the current version is not the last one in the upgrade sequence then return it, otherwise return null.
            if (UPGRADE_SEQUENCE.get(idx).compareTo(current) > 0) {
                return UPGRADE_SEQUENCE.get(idx);
            } else if (idx < UPGRADE_SEQUENCE.size() - 1) {
                return UPGRADE_SEQUENCE.get(idx + 1);
            } else {
                return null;
            }
        }
    }
}
