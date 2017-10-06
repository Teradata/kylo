package com.thinkbiganalytics.metadata.upgrade.v084;

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
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeException;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jcr.Node;

/**
 * Ensures that all categories have the new, mandatory feedRoleMemberships node.
 */
@Component("versionableFeedUpgradeAction084")
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class VersionableFeedUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(VersionableFeedUpgradeAction.class);

    @Inject
    private FeedProvider feedProvider;

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.8", "4", "");
    }

    @Override
    public void upgradeTo(final KyloVersion startingVersion) {
        log.info("Upgrading feeds as versionable for version: {}", startingVersion);

        feedProvider.getFeeds().forEach(feed -> {
            JcrFeed jcrFeed = (JcrFeed) feed;
            Node summaryNode = jcrFeed.getFeedSummary().get().getNode();
            try {
                summaryNode.addMixin("mix:versionable");
            } catch (Exception e) {
                log.error("Failed to set a feed as versionable: {}", feed.getName(), e);;
                throw new UpgradeException("Failed to set a feed summary node as versionable: " + summaryNode, e);
            }
        });
    }
}
