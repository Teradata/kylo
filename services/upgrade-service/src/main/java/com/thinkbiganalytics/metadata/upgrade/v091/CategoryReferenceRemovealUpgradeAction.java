package com.thinkbiganalytics.metadata.upgrade.v091;

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

import javax.inject.Inject;
import javax.jcr.Node;

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


/**
 * Removes category node reference property from all feeds.  A feed's category is now only accessed through
 * the feed node's parentage.
 */
@Component("categoryReferenceRemovealUpgradeAction091")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class CategoryReferenceRemovealUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(CategoryReferenceRemovealUpgradeAction.class);
    
    @Inject
    private FeedProvider feedProvider;
    
    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.9", "1", "");
    }

    @Override
    public void upgradeTo(final KyloVersion targetVersion) {
        log.info("Removing redundent category reference from feeds: {}", targetVersion);
        
        this.feedProvider.getFeeds().stream()
            .map(JcrFeed.class::cast)
            .forEach(feed -> {
                feed.getFeedSummary().ifPresent(summary -> {
                    Node node = summary.getNode();
                    JcrPropertyUtil.setProperty(node, "tba:category", null);
                });
            });
    }
}
