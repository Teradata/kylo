package com.thinkbiganalytics.feedmgr.service.feed;

import com.thinkbiganalytics.feedmgr.rest.model.EntityVersion;

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

import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.FeedVersions;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.rest.model.UserField;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.modeshape.versioning.VersionNotFoundException;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.security.action.Action;

import java.io.Serializable;
import java.security.AccessControlException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Common Feed Manager actions
 */
public interface FeedManagerFeedService {

    /**
     * Checks the current security context has been granted permission to perform the specified action(s)
     * on the feed with the specified feed ID.  If the feed does not exist then no check is made.
     *
     * @param id     the feed ID
     * @param action an action to check
     * @param more   any additional actions to check
     * @return true if the feed existed, otherwise false
     * @throws AccessControlException thrown if the feed exists and the action(s) checked are not permitted
     */
    boolean checkFeedPermission(String id, Action action, Action... more);

    /**
     * Return a feed matching its system category and system feed name
     *
     * @param categoryName a system category name
     * @param feedName     a system feed name
     * @return a feed matching its system category and system feed name
     */
    FeedMetadata getFeedByName(String categoryName, String feedName);

    /**
     * Return a feed matching the incoming id
     *
     * @param id a feed id
     * @return a feed matching the id, or null if not found
     */
    FeedMetadata getFeedById(String id);

    /**
     * Return a feed matching the feedId.
     *
     * @param refreshTargetTableSchema if true it will attempt to update the metadata of the destination table {@link FeedMetadata#table} with the real the destination
     * @return a feed matching the feedId
     */
    FeedMetadata getFeedById(String id, boolean refreshTargetTableSchema);

    /**
     * Get all versions of the feed with the specified ID.  The results will
     * have at least one version: the current feed version.  The results may
     * also contain the state of the version of each feed itself.
     * @param feedId the feed's ID
     * @param includeFeeds 
     * @return the feed versions
     */
    FeedVersions getFeedVersions(String feedId, boolean includeFeeds);

    /**
     * Get a version for the given feed and version ID.  The returned 
     * optional will be empty if no feed exists with the given ID.  A
     * VersionNotFoundException will 
     * @param feedId the feed ID
     * @param versionId the version ID
     * @param includeContent indicates whether the feed content should be included in the version
     * @return an optional feed version
     * @throws VersionNotFoundException if no version exists with the given ID
     */
    Optional<EntityVersion> getFeedVersion(String feedId, String versionId, boolean includeContent);

    /**
     * @return a list of all the feeds in the system
     */
    Collection<FeedMetadata> getFeeds();

    /**
     * Return a list of feeds, optionally returning a more verbose object populating all the templates and properties.
     * Verbose will return {@link FeedMetadata} objects, false will return {@link FeedSummary} objects
     *
     * @param verbose true will return {@link FeedMetadata} objects, false will return {@link FeedSummary} objects
     * @return a list of feed objects
     */
    Collection<? extends UIFeed> getFeeds(boolean verbose);

    /**
     * Return a list of feeds, optionally returning a more verbose object populating all the templates and properties.
     * Verbose will return {@link FeedMetadata} objects, false will return {@link FeedSummary} objects
     * <p>
     * The sized of the returned list will not be greater than the limit parameter, and the first element 
     * of the list will be the n'th feed in the list of all feeds as specified by the start parameter.
     *
     * @param verbose true will return {@link FeedMetadata} objects, false will return {@link FeedSummary} objects
     * @param pageable describes the page requested
     * @param filter TODO
     * @return a list of feed objects
     */
    Page<UIFeed> getFeeds(boolean verbose, Pageable pageable, String filter);
    
    /**
     * @return a list of feeds
     */
    List<FeedSummary> getFeedSummaryData();

    /**
     * Return a list of feeds in a given category
     *
     * @param categoryId the category to look at
     * @return a list of feeds in a given category
     */
    List<FeedSummary> getFeedSummaryForCategory(String categoryId);

    /**
     * Find all the feeds assigned to a given template
     *
     * @param registeredTemplateId a registered template id
     * @return all the feeds assigned to a given template
     */
    List<FeedMetadata> getFeedsWithTemplate(String registeredTemplateId);

    /**
     * Converts the specified feed id to a {@link Feed.ID} object.
     *
     * @param fid the feed id, usually a string
     * @return the {@link Feed.ID} object
     */
    Feed.ID resolveFeed(@Nonnull Serializable fid);

    /**
     * Create a new Feed in NiFi
     *
     * @param feedMetadata metadata about the feed
     * @return an object with status information about the newly created feed, or error information if unsuccessful
     */
    NifiFeed createFeed(FeedMetadata feedMetadata);

    /**
     * Deletes the specified feed.
     *
     * @param feedId the feed id
     * @throws RuntimeException if the feed cannot be deleted
     */
    void deleteFeed(@Nonnull String feedId);

    /**
     * Allows a feed's cleanup flow to run.
     *
     * @param feedId the feed id to be cleaned up
     * @throws RuntimeException if the metadata property cannot be set
     */
    void enableFeedCleanup(@Nonnull String feedId);

    /**
     * Change the state of the feed to be {@link FeedMetadata.STATE#ENABLED}
     *
     * @return a summary of the feed after being enabled
     */
    FeedSummary enableFeed(String feedId);

    /**
     * Change the state of the feed to be {@link FeedMetadata.STATE#DISABLED}
     *
     * @return a summary of the feed after being disabled
     */
    FeedSummary disableFeed(String feedId);


    void applyFeedSelectOptions(List<FieldRuleProperty> properties);

    /**
     * Gets the user-defined fields for feeds.
     *
     * @return the user-defined fields
     */
    @Nonnull
    Set<UserField> getUserFields();

    /**
     * Sets the user-defined fields for feeds.
     *
     * @param userFields the new set of user-defined fields
     */
    void setUserFields(@Nonnull Set<UserField> userFields);

    /**
     * Gets the user-defined fields for feeds within the specified category.
     *
     * @param categoryId the category id
     * @return the user-defined fields, if the category exists
     */
    @Nonnull
    Optional<Set<UserProperty>> getUserFields(@Nonnull String categoryId);


    /**
     * Update a given feeds datasources clearing its sources/destinations before revaluating the data
     * @param feedId the id of the feed rest model to update
     */
    void updateFeedDatasources(String feedId);

    /**
     * Iterate all of the feeds, clear all sources/destinations and reassign
     * Note this will be an expensive call if you have a lot of feeds
     */
    void updateAllFeedsDatasources();
}
