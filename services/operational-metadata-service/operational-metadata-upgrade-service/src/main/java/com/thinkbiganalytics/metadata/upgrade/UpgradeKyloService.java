package com.thinkbiganalytics.metadata.upgrade;

/*-
 * #%L
 * thinkbig-operational-metadata-upgrade-service
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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.app.KyloVersion;
import com.thinkbiganalytics.metadata.api.app.KyloVersionProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.jpa.feed.JpaOpsManagerFeed;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.ModeShapeAvailability;
import com.thinkbiganalytics.metadata.modeshape.common.ModeShapeAvailabilityListener;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedManagerFeed;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

/**
 * Created by sr186054 on 9/19/16.
 */
@Service
public class UpgradeKyloService implements ModeShapeAvailabilityListener {

    private static final Logger log = LoggerFactory.getLogger(UpgradeKyloService.class);

    @Inject
    private KyloVersionProvider kyloVersionProvider;

    @Inject
    private FeedManagerFeedProvider feedManagerFeedProvider;

    @Inject
    private FeedProvider feedProvider;

    @Inject
    private FeedManagerTemplateProvider feedManagerTemplateProvider;
    
    @Inject
    private FeedManagerCategoryProvider feedManagerCategoryProvider;

    @Inject
    OpsManagerFeedProvider opsManagerFeedProvider;

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    private AccessController accessController;

    @Inject
    private ModeShapeAvailability modeShapeAvailability;


    @PostConstruct
    private void init() {
        modeShapeAvailability.subscribe(this);
    }

    @Override
    public void modeShapeAvailable() {

        upgradeCheck();

    }

    public void upgradeCheck() {

        KyloVersion version = kyloVersionProvider.getKyloVersion();
        if (version == null || version.getMajorVersionNumber() == null || (version.getMajorVersionNumber() != null && version.getMajorVersionNumber().floatValue() < 0.4f)) {
            version = upgradeTo0_4_0();
        }
        ensureFeedTemplateFeedRelationships();
        // migrateUnusedFeedProperties();
            version = metadataAccess.commit(() -> {
                //ensure/update the version
                KyloVersion kyloVersion = kyloVersionProvider.updateToCurrentVersion();
                return kyloVersion;
            }, MetadataAccess.SERVICE);

        log.info("Upgrade check complete for Kylo {}", version.getVersion());


    }

    /**
     * Ensure the Feed Template has the relationships setup to its related feeds
     */
    private void ensureFeedTemplateFeedRelationships() {
        metadataAccess.commit(() -> {

            //ensure the templates have the feed relationships
            List<FeedManagerFeed> feeds = feedManagerFeedProvider.findAll();
            if (feeds != null) {
                feeds.stream().forEach(feed -> {
                    FeedManagerTemplate template = feed.getTemplate();
                    if (template != null) {
                        //ensure the template has feeds
                        if (template.getFeeds() == null || !template.getFeeds().contains(feed)) {
                            template.addFeed(feed);
                            feedManagerTemplateProvider.update(template);
                        }
                    }
                });

            }

            feedProvider.populateInverseFeedDependencies();

            return null;
        }, MetadataAccess.SERVICE);
    }

    /**
     * Migrate and remove or move any properties defined
     */
    private void migrateUnusedFeedProperties() {
        Set<String> propertiesToRemove = new HashSet<>();
        //propertiesToRemove.add('nametoremove');
        if (!propertiesToRemove.isEmpty()) {
            metadataAccess.commit(() -> {

                List<FeedManagerFeed> domainFeeds = feedManagerFeedProvider.findAll();
                for (FeedManagerFeed feedManagerFeed : domainFeeds) {

                    final PropertyIterator iterator;
                    try {
                        iterator = ((JcrFeedManagerFeed) feedManagerFeed).getNode().getProperties();
                    } catch (RepositoryException e) {
                        throw new MetadataRepositoryException("Failed to get properties for node: " + feedManagerFeed, e);
                    }
                    while (iterator.hasNext()) {
                        final Property property = iterator.nextProperty();
                        try {

                            if (propertiesToRemove.contains(property.getName())) {
                                property.remove();
                            }
                        } catch (Exception e) {

                        }
                    }
                }
                return null;
            }, MetadataAccess.SERVICE);
        }
    }





    public KyloVersion upgradeTo0_4_0() {

        return metadataAccess.commit(() -> metadataAccess.commit(() -> {
            
            for (FeedManagerCategory category : feedManagerCategoryProvider.findAll()) {
                // Ensure each category has an allowedActions (gets create if not present.)
                category.getAllowedActions();
            }

            //1 get all feeds defined in feed manager
            List<FeedManagerFeed> domainFeeds = feedManagerFeedProvider.findAll();
            Map<String, FeedManagerFeed> feedManagerFeedMap = new HashMap<>();
            if (domainFeeds != null && !domainFeeds.isEmpty()) {
                List<OpsManagerFeed.ID> opsManagerFeedIds = new ArrayList<OpsManagerFeed.ID>();
                for (FeedManagerFeed feedManagerFeed : domainFeeds) {
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
                for (FeedManagerFeed feed : feedManagerFeedMap.values()) {
                    String fullName = FeedNameUtil.fullName(feed.getCategory().getName(), feed.getName());
                    OpsManagerFeed.ID opsManagerFeedId = opsManagerFeedProvider.resolveId(feed.getId().toString());
                    OpsManagerFeed opsManagerFeed = new JpaOpsManagerFeed(opsManagerFeedId, fullName);
                    feedsToAdd.add(opsManagerFeed);
                }
                log.info("Synchronizing Feeds from Feed Manager. About to insert {} feed ids/names into Operations Manager", feedsToAdd.size());
                opsManagerFeedProvider.save(feedsToAdd);
            }

            //update the version
            return kyloVersionProvider.updateToCurrentVersion();
        }), MetadataAccess.SERVICE);
    }


}
