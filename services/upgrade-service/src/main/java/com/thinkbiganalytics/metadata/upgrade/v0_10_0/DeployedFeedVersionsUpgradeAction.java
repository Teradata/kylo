package com.thinkbiganalytics.metadata.upgrade.v0_10_0;

/*-
 * #%L
 * kylo-upgrade-service
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
import com.thinkbiganalytics.metadata.api.versioning.EntityVersion;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedProvider;
import com.thinkbiganalytics.metadata.modeshape.versioning.JcrEntityVersion;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.inject.Inject;


/**
 * In 0.10.0 the concept of a deployed Feed version is introduced to track which version of a 
 * particular feed is the one that is deployed; i.e. which version state was used to update NiFi.
 * <p>
 * With all feeds prior to 0.10.0, the latest feed version is the one that is deployed, and this action
 * initializes the reference to the latest version as the deployed one.
 */
@Component("deployedFeedVersionsUpgradeAction0.9.1")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class DeployedFeedVersionsUpgradeAction implements UpgradeAction {

    private static final Logger log = LoggerFactory.getLogger(DeployedFeedVersionsUpgradeAction.class);
    
    @Inject
    private JcrFeedProvider feedProvider;
    
    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.10", "0", "");
    }

    @Override
    public void upgradeTo(final KyloVersion targetVersion) {
        log.info("Marking all existing feeds as deployed: {}", targetVersion);
        
        this.feedProvider.getFeeds().stream()
            .map(JcrFeed.class::cast)
            .forEach(feed -> {
                feedProvider.findVersions(feed.getId(), false).stream()
                    .filter(ver -> ! ver.getName().equals(EntityVersion.DRAFT_NAME))
                    .findFirst()
                    .map(JcrEntityVersion.class::cast)
                    .ifPresent(ver -> feed.setDeployedVersion(ver.getVersion()));
            });
    }
}
