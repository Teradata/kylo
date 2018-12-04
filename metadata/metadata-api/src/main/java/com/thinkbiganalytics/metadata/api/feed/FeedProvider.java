package com.thinkbiganalytics.metadata.api.feed;

/*-
 * #%L
 * thinkbig-metadata-api
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

import com.thinkbiganalytics.metadata.api.BaseProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSet;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.versioning.EntityDraftVersionProvider;
import com.thinkbiganalytics.metadata.api.versioning.EntityVersion;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

public interface FeedProvider extends BaseProvider<Feed, Feed.ID>, EntityDraftVersionProvider<Feed, Feed.ID> {

    FeedSource ensureFeedSource(Feed.ID feedId, Datasource.ID dsId);
    
    FeedSource ensureFeedSource(Feed.ID feedId, DataSet.ID dsId);

    FeedSource ensureFeedSource(Feed.ID feedId, DataSet.ID dsId, boolean isSample);

    FeedDestination ensureFeedDestination(Feed.ID feedId, Datasource.ID dsId);
    
    FeedDestination ensureFeedDestination(Feed.ID feedId, DataSet.ID dsId);

    Feed ensureFeed(Category.ID categoryId, String feedSystemName);

    Feed ensureFeed(String categorySystemName, String feedSystemName);

    Feed ensureFeed(String categorySystemName, String feedSystemName, String descr);

    Feed ensureFeed(String categorySystemName, String feedSystemName, String descr, Datasource.ID destId);

    Feed ensureFeed(String categorySystemName, String feedSystemName, String descr, Datasource.ID srcId, Datasource.ID destId);

    Feed createPrecondition(Feed.ID feedId, String descr, List<Metric> metrics);

    PreconditionBuilder buildPrecondition(Feed.ID feedId);
    
    List<? extends Feed> findPreconditionedFeeds();

    Feed findBySystemName(String systemName);

    Feed findBySystemName(String categorySystemName, String systemName);

    FeedCriteria feedCriteria();

    Feed getFeed(Feed.ID id);

    List<Feed> getFeedsForDataHistoryReindexing();

    List<? extends Feed> getFeeds();

    List<Feed> getFeeds(FeedCriteria criteria);

    /**
     * Moves the specified feed from its current category to a new category.
     */
    Feed moveFeed(Feed feed, Category.ID toCatId);
    
    /**
     * Moves the specified feed from its current category to a new category.
     */
    Feed moveFeed(Feed feed, Category toCat);

    /**
     * Changes the system name of a feed
     */
    Feed changeSystemName(Feed feed, String newName);

    Feed addDependent(Feed.ID targetId, Feed.ID dependentId);

    Feed removeDependent(Feed.ID feedId, Feed.ID depId);

    void populateInverseFeedDependencies();


    void removeFeedSources(Feed.ID feedId);

    void removeFeedSource(Feed.ID feedId, Datasource.ID dsId);
    
    void removeFeedSource(Feed.ID feedId, DataSet.ID dsId);

    void removeFeedDestination(Feed.ID feedId, Datasource.ID dsId);

    void removeFeedDestinations(Feed.ID feedId);

//    FeedSource getFeedSource(FeedSource.ID id);
//    FeedDestination getFeedDestination(FeedDestination.ID id);

    Feed.ID resolveFeed(Serializable fid);

//    FeedSource.ID resolveSource(Serializable sid);
//    FeedDestination.ID resolveDestination(Serializable sid);

    boolean enableFeed(Feed.ID id);

    boolean disableFeed(Feed.ID id);

    /**
     * Deletes the feed with the specified id.
     *
     * @param feedId the feed id to be deleted
     * @throws RuntimeException if the feed cannot be deleted
     */
    void deleteFeed(Feed.ID feedId);
    
    void setDeployed(Feed.ID feedId, EntityVersion.ID versionId);

    Feed updateFeedServiceLevelAgreement(Feed.ID feedId, ServiceLevelAgreement sla);

    /**
     * Merge properties and return the newly merged properties
     */
    Map<String, Object> mergeFeedProperties(Feed.ID feedId, Map<String, Object> properties);

    Map<String, Object> replaceProperties(Feed.ID feedId, Map<String, Object> properties);

    /**
     * Gets the user fields for all feeds.
     *
     * @return user field descriptors
     * @since 0.4.0
     */
    @Nonnull
    Set<UserFieldDescriptor> getUserFields();

    /**
     * Sets the user fields for all feeds.
     *
     * @param userFields user field descriptors
     * @since 0.4.0
     */
    void setUserFields(@Nonnull Set<UserFieldDescriptor> userFields);

    List<? extends Feed> findByTemplateId(FeedManagerTemplate.ID templateId);

    List<? extends Feed> findByCategoryId(Category.ID categoryId);
    
    Optional<EntityVersion<Feed.ID, Feed>> findDeployedVersion(Feed.ID feedId, boolean includeContent);

    // TODO Methods to add policy info to source
}
