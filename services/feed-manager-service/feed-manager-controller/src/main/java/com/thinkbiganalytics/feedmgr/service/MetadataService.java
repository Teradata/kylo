package com.thinkbiganalytics.feedmgr.service;

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

import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.rest.model.EntityVersion;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.FeedVersions;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.rest.model.UserFieldCollection;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.metadata.modeshape.versioning.VersionNotFoundException;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.security.action.Action;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.AccessControlException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Provides access to category, feed, and template metadata.
 */
@Service
public interface MetadataService {
    
    /**
     * Checks the current security context has been granted permission to perform the specified action(s) 
     * on the feed with the specified feed ID.  If the feed does not exist then no check is made.
     * @param id the feed ID
     * @param action an action to check
     * @param more any additional actions to check
     * @return true if the feed existed, otherwise false
     * @throws AccessControlException thrown if the feed exists and the action(s) checked are not permitted
     */
    boolean checkFeedPermission(String id, Action action, Action... more);

    /**
     * Register a template, save it, and return
     *
     * @param registeredTemplate a template to register/update
     * @return the registered template
     */
    RegisteredTemplate registerTemplate(RegisteredTemplate registeredTemplate);

    /**
     * Return all properties registered for a template
     *
     * @param templateId a template id
     * @return all properties registered for a template
     */
    List<NifiProperty> getTemplateProperties(String templateId);

    /**
     * Deletes a template
     *
     * @param templateId a registered template id
     */
    void deleteRegisteredTemplate(String templateId);

    /**
     * Return all registered templates
     *
     * @return a list of all registered templates
     */
    List<RegisteredTemplate> getRegisteredTemplates();

    /**
     * Finds a template by its name
     * @param templateName the name of the template to look for
     * @return the template
     */
    RegisteredTemplate findRegisteredTemplateByName(final String templateName);

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
     * @throws FeedCleanupFailedException  if the cleanup flow was started but failed to complete successfully
     * @throws FeedCleanupTimeoutException if the cleanup flow was started but failed to complete in the allotted time
     * @throws IllegalArgumentException    if the feed does not exist
     * @throws IllegalStateException       if there are dependent feeds
     * @throws NifiClientRuntimeException  if the feed cannot be deleted from NiFi
     * @throws RuntimeException            if the feed could not be deleted for any other reason
     */
    void deleteFeed(@Nonnull String feedId);

    /**
     * Change the state of the feed to be {@link FeedMetadata.STATE#ENABLED}
     *
     * @param feedId the feed id
     * @return a summary of the feed after being enabled
     */
    FeedSummary enableFeed(String feedId);

    /**
     * Change the state of the feed to be {@link FeedMetadata.STATE#DISABLED}
     *
     * @param feedId the feed id
     * @return a summary of the feed after being disabled
     */
    FeedSummary disableFeed(String feedId);

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
     * Gets a page worth of feeds, optionally returning a more verbose object populating all the templates and properties.
     * Verbose will return {@link FeedMetadata} objects, false will return {@link FeedSummary} objects
     *
     * @param verbose true will return {@link FeedMetadata} objects, false will return {@link FeedSummary} objects
     * @param pageable describes the page to be returned
     * @param filter TODO
     * @return a page of feeds determined by the values of limit and start
     */
    Page<UIFeed> getFeedsPage(boolean verbose, Pageable pageable, String filter);
    
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
     * Return a feed matching on its system category name and  system feed name
     *
     * @param categoryName the system name for a category
     * @param feedName     the system feed name
     * @return a feed matching on its system category name and  system feed name
     */
    FeedMetadata getFeedByName(String categoryName, String feedName);

    /**
     * Return a feed matching the feedId
     *
     * @param feedId the feed id
     * @return a feed matching the feedId, null if not found
     */
    FeedMetadata getFeedById(String feedId);

    /**
     * Return a feed matching the feedId.
     *
     * @param feedId                   the feed id
     * @param refreshTargetTableSchema if true it will attempt to update the metadata of the destination table {@link FeedMetadata#table} with the real the destination
     * @return a feed matching the feedId
     */
    FeedMetadata getFeedById(String feedId, boolean refreshTargetTableSchema);

    /**
     * Return the categories
     *
     * @return the categories
     */
    Collection<FeedCategory> getCategories();

    /**
     * Returns the categories
     * @param includeFeedDetails true to return the list of related feeds.  if true this will be a slower call
     * @return the categories
     */
    Collection<FeedCategory> getCategories(boolean includeFeedDetails);

    /**
     * Return a category matching a system name
     *
     * @param name a category system name
     * @return the matching category, or null if not found
     */
    FeedCategory getCategoryBySystemName(final String name);

    /**
     * Return a category via its id
     * @param categoryId category id
     * @return the matching category, or null if not found
     */
    FeedCategory getCategoryById(final String categoryId);

    /**
     * save a category
     *
     * @param category a category to save
     */
    void saveCategory(FeedCategory category);

    /**
     * Delete a category
     *
     * @return true if deleted, false if not
     * @throws InvalidOperationException if unable to delete (categories cannot be deleted if there are feeds assigned to them)
     */
    boolean deleteCategory(String categoryId) throws InvalidOperationException;

    /**
     * Gets the user-defined fields for all categories.
     *
     * @return the user-defined fields
     */
    @Nonnull
    Set<UserProperty> getCategoryUserFields();

    /**
     * Gets the user-defined fields for all feeds within the specified category.
     *
     * @param categoryId the category id
     * @return the user-defined fields, if the category exists
     */
    @Nonnull
    Optional<Set<UserProperty>> getFeedUserFields(@Nonnull String categoryId);

    /**
     * Get all versions of the feed with the specified ID.  The results will
     * have at least one version: the current feed version.  The results may
     * also contain the state of the version of each feed itself.
     * @param feedId the feed's ID
     * @param includeContent indicates whether the feed content should be included in the results
     * @return the feed versions
     */
    FeedVersions getFeedVersions(String feedId, boolean includeContent);

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
     * Gets the user-defined fields for all categories and feeds.
     *
     * @return the user-defined fields
     */
    @Nonnull
    UserFieldCollection getUserFields();

    /**
     * Sets the user-defined fields for all categories and feeds.
     *
     * @param userFields the new user-defined fields
     */
    void setUserFields(@Nonnull UserFieldCollection userFields);


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
