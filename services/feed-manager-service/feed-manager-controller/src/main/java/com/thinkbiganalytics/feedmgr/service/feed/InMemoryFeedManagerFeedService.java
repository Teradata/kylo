package com.thinkbiganalytics.feedmgr.service.feed;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.feedmgr.rest.model.EntityVersion;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.FeedVersions;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.rest.model.UserField;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.feedmgr.service.FileObjectPersistence;
import com.thinkbiganalytics.feedmgr.service.category.FeedManagerCategoryService;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.rest.model.LabelValue;
import com.thinkbiganalytics.security.action.Action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * In memory implementation
 */
public class InMemoryFeedManagerFeedService implements FeedManagerFeedService {

    @Inject
    private LegacyNifiRestClient nifiRestClient;

    @Inject
    private FeedManagerCategoryService categoryProvider;

    @Inject
    private FeedManagerTemplateService templateProvider;

    private Map<String, FeedMetadata> feeds = new HashMap<>();

    @PostConstruct
    private void postConstruct() {
        Collection<FeedMetadata> savedFeeds = FileObjectPersistence.getInstance().getFeedsFromFile();
        if (savedFeeds != null) {
            Long maxId = 0L;
            for (FeedMetadata feed : savedFeeds) {

                //update the category mappings
                String categoryId = feed.getCategory().getId();
                FeedCategory category = categoryProvider.getCategoryById(categoryId);
                feed.setCategory(category);
                category.addRelatedFeed(new FeedSummary(feed));
                //add it to the map
                feeds.put(feed.getId(), feed);
            }

            loadSavedFeedsToMetaClientStore();
        }
    }

    @Override
    public boolean checkFeedPermission(String id, Action action, Action... more) {
        // Permission checking not currently implemented for the in-memory implementation
        return true;
    }

    public Collection<FeedMetadata> getFeeds() {
        return feeds.values();
    }

    public Collection<? extends UIFeed> getFeeds(boolean verbose) {
        if (verbose) {
            return getFeeds();
        } else {
            return getFeedSummaryData();
        }
    }
    
    @Override
    public Page<UIFeed> getFeeds(boolean verbose, Pageable pageable, String filter) {
        Collection<? extends UIFeed> allFeeds = getFeeds(verbose);
        List<UIFeed> pagedFeeds = getFeeds(verbose).stream()
                        .skip(Math.max(pageable.getOffset() - 1, 0))
                        .limit(pageable.getPageSize())
                        .collect(Collectors.toList());
        return new PageImpl<>(pagedFeeds, 
                             new PageRequest(pageable.getOffset() / pageable.getPageSize(), pageable.getPageSize()), 
                             allFeeds.size());
    }

    public List<FeedSummary> getFeedSummaryData() {
        List<FeedSummary> summaryList = new ArrayList<>();
        if (feeds != null && !feeds.isEmpty()) {
            for (FeedMetadata feed : feeds.values()) {
                summaryList.add(new FeedSummary(feed));
            }
        }
        return summaryList;
    }


    public List<FeedSummary> getFeedSummaryForCategory(String categoryId) {
        List<FeedSummary> summaryList = new ArrayList<>();
        FeedCategory category = categoryProvider.getCategoryById(categoryId);
        if (category != null && category.getFeeds() != null) {
            summaryList.addAll(category.getFeeds());
        }
        return summaryList;
    }


    @Override
    public FeedMetadata getFeedByName(final String categoryName, final String feedName) {

        if (feeds != null && !feeds.isEmpty()) {
            return Iterables.tryFind(feeds.values(), new Predicate<FeedMetadata>() {
                @Override
                public boolean apply(FeedMetadata metadata) {

                    return metadata.getFeedName().equalsIgnoreCase(feedName) && metadata.getCategoryName().equalsIgnoreCase(categoryName);
                }
            }).orNull();
        }
        return feeds.get(feedName);
    }

    @Override
    public FeedMetadata getFeedById(String id) {
        return getFeedById(id, false);
    }

    @Override
    public FeedMetadata getFeedById(String id, boolean refreshTargetTableSchema) {

        if (feeds != null && !feeds.isEmpty()) {
            FeedMetadata feed = feeds.get(id);
            if (feed != null) {
                //get the latest category data
                FeedCategory category = categoryProvider.getCategoryById(feed.getCategory().getId());
                feed.setCategory(category);

                //set the template to the feed

                RegisteredTemplate registeredTemplate = templateProvider.getRegisteredTemplate(feed.getTemplateId());

                if (registeredTemplate != null) {
                    RegisteredTemplate copy = new RegisteredTemplate(registeredTemplate);
                    copy.getProperties().clear();
                    feed.setRegisteredTemplate(copy);
                    feed.setTemplateId(copy.getNifiTemplateId());
                }

                return feed;
            }
        }

        return null;
    }

    @Override
    public FeedVersions getFeedVersions(String feedId, boolean includeContent) {
        FeedVersions versions = new FeedVersions(feedId);
        FeedMetadata feed = getFeedById(feedId);
        
        if (feed != null) {
            EntityVersion version = versions.addNewVersion(UUID.randomUUID().toString(), "v1.0", feed.getCreateDate());
            if (includeContent) {
                version.setEntity(feed);
            }
        }
        return versions;
    }
    
    @Override
    public Optional<EntityVersion> getFeedVersion(String feedId, String versionId, boolean includeContent) {
        FeedMetadata feed = getFeedById(feedId);
        EntityVersion version = null;
        
        if (feed != null) {
            version = new EntityVersion(UUID.randomUUID().toString(), "v1.0", feed.getCreateDate());
            if (includeContent) {
                version.setEntity(feed);
            }
        }
        
        return Optional.ofNullable(version);
    }

    @Override
    public List<FeedMetadata> getFeedsWithTemplate(final String registeredTemplateId) {
        return Lists.newArrayList(Iterables.filter(feeds.values(), new Predicate<FeedMetadata>() {
            @Override
            public boolean apply(FeedMetadata feed) {
                return feed.getTemplateId().equalsIgnoreCase(registeredTemplateId);
            }
        }));
    }

    @Override
    public Feed.ID resolveFeed(@Nonnull Serializable fid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enableFeedCleanup(@Nonnull String feedId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Needed to rewire feeds to ids in the server upon server start since the Feed Metadata store is in memory now
     */
    private void loadSavedFeedsToMetaClientStore() {
        for (FeedMetadata feedMetadata : feeds.values()) {
            feedMetadata.setFeedId(null);
            //  saveToMetadataStore(feedMetadata);
        }
    }


    public void saveFeed(FeedMetadata feed) {
        if (feed.getId() == null || !feeds.containsKey(feed.getId())) {
            feed.setId(UUID.randomUUID().toString());
            feed.setVersion(new Long(1));
        } else {
            FeedMetadata previousFeed = feeds.get(feed.getId());
            feed.setId(previousFeed.getId());
            feed.setVersion(previousFeed.getVersion() + 1L);
        }

        //match up the related category
        String categoryId = feed.getCategory().getId();
        FeedCategory category = null;
        if (categoryId != null) {
            category = categoryProvider.getCategoryById(categoryId);
        }
        if (category == null) {
            final String categoryName = feed.getCategory().getSystemName();
            category = categoryProvider.getCategoryBySystemName(categoryName);
            feed.setCategory(category);
        }
        if (category != null) {
            category.addRelatedFeed(new FeedSummary(feed));
        }

        //  saveToMetadataStore(feed);
        feeds.put(feed.getId(), feed);
        FileObjectPersistence.getInstance().writeFeedsToFile(feeds.values());
    }

    @Override
    public void deleteFeed(@Nonnull String feedId) {
        feeds.remove(feedId);
    }

    @Override
    public FeedSummary enableFeed(String feedId) {
        FeedMetadata feedMetadata = getFeedById(feedId);
        if (feedMetadata != null) {
            feedMetadata.setState("ENABLED");
            return new FeedSummary(feedMetadata);
        }
        return null;
    }

    @Override
    public FeedSummary disableFeed(String feedId) {
        FeedMetadata feedMetadata = getFeedById(feedId);
        if (feedMetadata != null) {
            feedMetadata.setState("DISABLED");
            return new FeedSummary(feedMetadata);
        }
        return null;
    }

    @Override
    public void applyFeedSelectOptions(List<FieldRuleProperty> properties) {
        if (properties != null && !properties.isEmpty()) {
            List<FeedSummary> feedSummaries = getFeedSummaryData();
            List<LabelValue> feedSelection = new ArrayList<>();
            for (FeedSummary feedSummary : feedSummaries) {
                feedSelection.add(new LabelValue(feedSummary.getCategoryAndFeedDisplayName(), feedSummary.getCategoryAndFeedSystemName()));
            }
            for (FieldRuleProperty property : properties) {
                property.setSelectableValues(feedSelection);
                if (property.getValues() == null) {
                    property.setValues(new ArrayList<>()); // reset the intial values to be an empty arraylist
                }
            }
        }
    }

    @Nonnull
    @Override
    public Set<UserField> getUserFields() {
        return Collections.emptySet();
    }

    @Override
    public void setUserFields(@Nonnull Set<UserField> userFields) {
    }

    @Nonnull
    @Override
    public Optional<Set<UserProperty>> getUserFields(@Nonnull String categoryId) {
        return Optional.of(Collections.emptySet());
    }

    @Override
    public NifiFeed createFeed(FeedMetadata feedMetadata) {
        saveFeed(feedMetadata);
        NifiFeed nifiFeed = new NifiFeed();
        nifiFeed.setFeedMetadata(feedMetadata);
        nifiFeed.setSuccess(true);
        return nifiFeed;
    }

    @Override
    public void updateFeedDatasources(String feedId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAllFeedsDatasources() {
        throw new UnsupportedOperationException();
    }
}
