package com.thinkbiganalytics.metadata.upgrade.v040;

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
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.jpa.feed.JpaOpsManagerFeed;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

@Component("feedOpspgradeAction040")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class FeedOpsUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(FeedOpsUpgradeAction.class);

    @Inject
    private FeedProvider feedProvider;
    @Inject
    private CategoryProvider categoryProvider;
    @Inject
    private OpsManagerFeedProvider opsManagerFeedProvider;

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.4", "0", "");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.upgrade.UpgradeState#upgradeFrom(com.thinkbiganalytics.metadata.api.app.KyloVersion)
     */
    @Override
    public void upgradeTo(KyloVersion startingVersion) {
        log.info("Upgrading from version: " + startingVersion);
        
        for (Category category : categoryProvider.findAll()) {
            // Ensure each category has an allowedActions (gets create if not present.)
            category.getAllowedActions();
        }

        // get all feeds defined in feed manager
        List<Feed> domainFeeds = feedProvider.findAll();
        Map<String, Feed> feedManagerFeedMap = new HashMap<>();
        if (domainFeeds != null && !domainFeeds.isEmpty()) {
            List<OpsManagerFeed.ID> opsManagerFeedIds = new ArrayList<OpsManagerFeed.ID>();
            for (Feed feedManagerFeed : domainFeeds) {
                opsManagerFeedIds.add(opsManagerFeedProvider.resolveId(feedManagerFeed.getId().toString()));
                feedManagerFeedMap.put(feedManagerFeed.getId().toString(), feedManagerFeed);

                // Ensure each feed has an allowedActions (gets create if not present.)
                feedManagerFeed.getAllowedActions();
            }
            //find those that match
            List<? extends OpsManagerFeed> opsManagerFeeds = opsManagerFeedProvider.findByFeedIds(opsManagerFeedIds);
            if (opsManagerFeeds != null) {
                for (OpsManagerFeed opsManagerFeed : opsManagerFeeds) {
                    feedManagerFeedMap.remove(opsManagerFeed.getId().toString());
                }
            }

            List<OpsManagerFeed> feedsToAdd = new ArrayList<>();
            for (Feed feed : feedManagerFeedMap.values()) {
                String fullName = FeedNameUtil.fullName(feed.getCategory().getSystemName(), feed.getName());
                OpsManagerFeed.ID opsManagerFeedId = opsManagerFeedProvider.resolveId(feed.getId().toString());
                OpsManagerFeed opsManagerFeed = new JpaOpsManagerFeed(opsManagerFeedId, fullName);
                feedsToAdd.add(opsManagerFeed);
            }
            log.info("Synchronizing Feeds from Feed Manager. About to insert {} feed ids/names into Operations Manager", feedsToAdd.size());
            opsManagerFeedProvider.save(feedsToAdd);
        }
    }
}
