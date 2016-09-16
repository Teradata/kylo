package com.thinkbiganalytics.feedmgr.service;

import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.rest.model.UserFieldCollection;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.springframework.stereotype.Service;

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

    void registerTemplate(RegisteredTemplate registeredTemplate);

    List<NifiProperty> getTemplateProperties(String templateId);

    RegisteredTemplate getRegisteredTemplate(String templateId);

    RegisteredTemplate getRegisteredTemplateByName(String templateName);

    RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId);

    RegisteredTemplate getRegisteredTemplateForNifiProperties(final String nifiTemplateId, final String nifiTemplateName);

    void deleteRegisteredTemplate(String templateId);

    // List<String> getRegisteredTemplateIds();

    List<RegisteredTemplate> getRegisteredTemplates();

    NifiFeed createFeed(FeedMetadata feedMetadata);

    void saveFeed(FeedMetadata feed);

    /**
     * Deletes the specified feed.
     *
     * @param feedId the feed id
     * @throws FeedCleanupFailedException if the cleanup flow was started but failed to complete successfully
     * @throws FeedCleanupTimeoutException if the cleanup flow was started but failed to complete in the allotted time
     * @throws IllegalArgumentException if the feed does not exist
     * @throws NifiClientRuntimeException if the feed cannot be deleted from NiFi
     * @throws RuntimeException if the feed could not be deleted for any other reason
     */
    void deleteFeed(@Nonnull String feedId);

    FeedSummary enableFeed(String feedId);

    FeedSummary disableFeed(String feedId);

    Collection<FeedMetadata> getFeeds();

    Collection<? extends UIFeed> getFeeds(boolean verbose);

    List<FeedSummary> getFeedSummaryData();

    List<FeedSummary> getFeedSummaryForCategory(String categoryId);

    FeedMetadata getFeedByName(String categoryName, String feedName);

    FeedMetadata getFeedById(String feedId);

    FeedMetadata getFeedById(String feedId, boolean refreshTargetTableSchema);

    List<FeedMetadata> getReusableFeeds();

    Collection<FeedCategory> getCategories();

    FeedCategory getCategoryBySystemName(final String name);

    void saveCategory(FeedCategory category);

    boolean deleteCategory(String categoryId) throws InvalidOperationException;

    /**
     * Gets the user-defined fields for all feeds within the specified category.
     *
     * @param categoryId the category id
     * @return the user-defined fields, if the category exists
     */
    @Nonnull
    Optional<Set<UserProperty>> getFeedUserFields(@Nonnull String categoryId);

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
}
