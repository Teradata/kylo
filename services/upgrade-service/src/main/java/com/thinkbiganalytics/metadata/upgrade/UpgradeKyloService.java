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

import javax.inject.Inject;

public class UpgradeKyloService {

    private static final Logger log = LoggerFactory.getLogger(UpgradeKyloService.class);

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
    
    
    /**
     * Upgrades to the next Kylo version relative to the current version.
     * @return true if after this call upgrades are complete and Kylo is up-to-date.
     */
    public boolean upgradeNext() {
        if (this.upgradeActions.isPresent()) {
            Optional<KyloVersion> nextVersion = metadataAccess.read(() -> {
                KyloVersion current = versionProvider.getCurrentVersion();
                return getNextVersion(current);
            }, MetadataAccess.SERVICE);
            
            return nextVersion
                .map(version ->
                    metadataAccess.commit(() -> {
                        this.upgradeActions.get().stream()
                            .filter(a -> a.isTargetVersion(version))
                            .forEach(a -> a.upgradeFrom(version));
                        return false;
                    }, MetadataAccess.SERVICE))
                .orElse(true);
        } else {
            // TODO No actions: update to latest version
            return true;
        }
    }

    private Optional<KyloVersion> getNextVersion(KyloVersion current) {
        int idx = UPGRADE_SEQUENCE.indexOf(current);
        return idx == UPGRADE_SEQUENCE.size() - 1 ? Optional.empty() : Optional.of(UPGRADE_SEQUENCE.get(idx + 1));
    }
}
