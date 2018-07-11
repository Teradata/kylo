package com.thinkbiganalytics.metadata.upgrade.v092;

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
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jcr.Node;


/**
 * in 0.9.2 the concept of a Feed Mode is introduced to track DRAFT feeds vs COMPLETE feeds.
 * All feeds prior to 0.9.2 dont have the 'mode' property and thus should be treated as 'COMPLETE'
 * Sets the JCRFeed.feedDetails.mode = 'COMPLETE' for all feeds
 */
@Component("initializeFeedModePropertyUpgradeAction091")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class InitializeFeedModePropertyUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(InitializeFeedModePropertyUpgradeAction.class);
    
    @Inject
    private FeedProvider feedProvider;
    
    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.9", "2", "");
    }

    @Override
    public void upgradeTo(final KyloVersion targetVersion) {
        log.info("Set the initialize JcrFeed.mode value to 'COMPLETE' for all existing feeds: {}", targetVersion);
        
        this.feedProvider.getFeeds().stream()
            .map(JcrFeed.class::cast)
            .forEach(feed -> {
                feed.getFeedData().ifPresent(data -> {
                    data.setMode(Feed.Mode.COMPLETE);
                });
            });
    }
}
