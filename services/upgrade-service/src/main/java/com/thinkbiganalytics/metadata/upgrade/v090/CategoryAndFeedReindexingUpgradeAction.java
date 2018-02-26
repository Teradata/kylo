package com.thinkbiganalytics.metadata.upgrade.v090;

/*-
 * #%L
 * kylo-upgrade-service
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeException;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.jcr.Node;

/**
 * Upgrade action to support reindexing
 * Ensures that all categories and feeds have flag to allow/stop metadata indexing
 * Ensures that all feeds have flag to track history data reindexing
 */
@Component("categoryAndFeedReindexUpgradeAction90")
@Order(Ordered.HIGHEST_PRECEDENCE - 10)
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class CategoryAndFeedReindexingUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(CategoryAndFeedReindexingUpgradeAction.class);

    @Inject
    private FeedProvider feedProvider;

    @Inject
    private CategoryProvider categoryProvider;

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.9", "0", "");
    }

    @Override
    public void upgradeTo(KyloVersion startingVersion) {
        log.info("****************** Start: Category and Feed Reindexing Support Upgrade Action ****************");

        log.info("[REF.A] Upgrading feeds to support (1) allowing/stopping metadata indexing, AND (2) reindexing of history data for version: {}...", startingVersion);
        log.info("Total feeds found for upgrading: {}", feedProvider.getFeeds().size());
        if (feedProvider.getFeeds().isEmpty()) {
            log.warn("No feeds have been found for upgrade!");
        }

        feedProvider.getFeeds().forEach(feed -> {
            JcrFeed jcrFeed = (JcrFeed) feed;

            if ((jcrFeed.getFeedSummary().isPresent()) && (jcrFeed.getFeedData().isPresent())) {
                Node summaryNode = jcrFeed.getFeedSummary().get().getNode();
                Node feedDataNode = jcrFeed.getFeedData().get().getNode();

                try {
                    summaryNode.addMixin("tba:indexControlled");
                    summaryNode.setProperty("tba:allowIndexing", "Y");
                    if(!JcrUtil.hasNode(feedDataNode, "tba:historyReindexing")) {
                        log.info("Feed with id [{}] in category [{}] having name [{}] requires history reindexing support. Adding it.", jcrFeed.getId(), jcrFeed.getCategory().getDisplayName(),jcrFeed.getName());
                        feedDataNode.addNode("tba:historyReindexing", "tba:historyReindexing");
                    } else {
                        log.info("Feed with id [{}] in category [{}] having name [{}] already has history reindexing support. Skipping step to add again.",jcrFeed.getId(), jcrFeed.getCategory().getDisplayName(),jcrFeed.getName());
                    }
                    log.info("Upgraded feed with id [{}] in category [{}] having name [{}]", jcrFeed.getId(), jcrFeed.getCategory().getDisplayName(),jcrFeed.getName());
                } catch (Exception e) {
                    log.error("Failed to configure feed {} to (1) support allowing/stopping metadata indexing, AND/OR (2) support reindexing of history data: {}", ((JcrFeed) feed).getName(), e);
                    throw new UpgradeException("Failed to configure feed to (1) support allowing/stopping metadata indexing, AND/OR (2) support reindexing of history data: " + summaryNode, e);
                }
            } else {
                log.error("Failed to get summary and/or data node for feed: {}", ((JcrFeed) feed).getName());
                throw new UpgradeException("Failed to get summary and/or data node for feed:" + ((JcrFeed) feed).getName());
            }
        });
        log.info("Completed [REF.A]: Configured feeds to support (1) allowing/stopping metadata indexing, AND (2) reindexing of history data for version: {}", startingVersion);

        log.info("[REF.B] Upgrading categories to support allowing/stopping metadata indexing for version: {}...", startingVersion);
        log.info("Total categories found for upgrading: {}", categoryProvider.findAll().size());
        if (categoryProvider.findAll().isEmpty()) {
            log.warn("No categories have been found for for upgrade!");
        }

        categoryProvider.findAll().forEach(category -> {
            JcrCategory jcrCategory = (JcrCategory) category;
            Node categoryNode = jcrCategory.getNode();
            try {
                categoryNode.addMixin("tba:indexControlled");
                categoryNode.setProperty("tba:allowIndexing", "Y");
                log.info("Upgraded category with id [{}] having name [{}]", jcrCategory.getId(), jcrCategory.getDisplayName());
            } catch (Exception e) {
                log.error("Failed to configure category {} to support allowing/stopping metadata indexing: {}", ((JcrCategory) category).getDisplayName(), e);
                throw new UpgradeException("Failed to configure category to support allowing/stopping metadata indexing: " + categoryNode, e);
            }
        });
        log.info("Completed [REF.B]: Configured categories to support allowing/stopping metadata indexing for version: {}", startingVersion);
        log.info("****************** End: Category and Feed Reindexing Support Upgrade Action ****************");
    }
}
