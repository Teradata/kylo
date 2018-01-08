package com.thinkbiganalytics.metadata.upgrade.v0841;

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
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.versioning.EntityVersion;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

/**
 * Remove any records kylo.FEED entries that have the same name, but dont exist in Modeshape
 */
@Component("removeDuplicateOpsManagerFeedsUpgradeAction084")
@Order(1)
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class RemoveDuplicateOpsManagerFeedsUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(RemoveDuplicateOpsManagerFeedsUpgradeAction.class);

    @Inject
    private OpsManagerFeedProvider opsManagerFeedProvider;

    @Inject
    private FeedProvider feedProvider;

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.8", "4", "1");
    }

    @Override
    public void upgradeTo(final KyloVersion startingVersion) {
        log.info("remove duplicate ops manager feeds from version: {}", startingVersion);

        List<? extends OpsManagerFeed> feeds = opsManagerFeedProvider.findFeedsWithSameName();
        if(feeds != null){
            final List<OpsManagerFeed> feedsToDelete = new ArrayList<>();
            final Map<String,OpsManagerFeed> feedsToKeep = new HashMap<>();

            feeds.stream().forEach(feed -> {
                log.info("Found duplicate feed {} - {} ",feed.getId(),feed.getName());
                Feed jcrFeed = feedProvider.getFeed(feed.getId());
                if(jcrFeed  == null){
                    feedsToDelete.add(feed);
                }
                else {
                    feedsToKeep.put(feed.getName(),feed);
                }

            });

            feedsToDelete.stream().forEach(feed -> {
                OpsManagerFeed feedToKeep = feedsToKeep.get(feed.getName());
                if(feedToKeep != null) {
                    //remove it
                    log.info("Unable to find feed {} - {} in JCR metadata.  A feed with id of {} already exists with this same name {}.  Attempt to remove its data from Operations Manager", feed.getId(), feed.getName(), feedToKeep.getId(),feedToKeep.getName());
                    opsManagerFeedProvider.delete(feed.getId());
                }
            });
        }

    }
}
