package com.thinkbiganalytics.feedmgr.service.feed;

import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.rest.model.UserField;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

public interface FeedManagerFeedService {

    List<FeedMetadata> getReusableFeeds();

    FeedMetadata getFeedByName(String categoryName, String feedName);

    FeedMetadata getFeedById(String id);

    FeedMetadata getFeedById(String id, boolean refreshTargetTableSchema);

    Collection<FeedMetadata> getFeeds();

    Collection<? extends UIFeed> getFeeds(boolean verbose);

    List<FeedSummary> getFeedSummaryData();

    List<FeedSummary> getFeedSummaryForCategory(String categoryId);

    List<FeedMetadata> getFeedsWithTemplate(String registeredTemplateId);

    /**
     * Converts the specified feed id to a {@link Feed.ID} object.
     *
     * @param fid the feed id, usually a string
     * @return the {@link Feed.ID} object
     */
    Feed.ID resolveFeed(@Nonnull Serializable fid);

    NifiFeed createFeed(FeedMetadata feedMetadata);

    void saveFeed(FeedMetadata feed);

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

    FeedSummary enableFeed(String feedId);

    FeedSummary disableFeed(String feedId);

    void updateFeedsWithTemplate(String oldTemplateId, String newTemplateId);

    void applyFeedSelectOptions(List<FieldRuleProperty> properties);

    /**
     * Gets the user-defined fields for feeds.
     *
     * @return the user-defined fields
     */
    @Nonnull
    Set<UserField> getUserFields();

    /**
     * Gets the user-defined fields for feeds within the specified category.
     *
     * @param categoryId the category id
     * @return the user-defined fields, if the category exists
     */
    @Nonnull
    Optional<Set<UserProperty>> getUserFields(@Nonnull String categoryId);

    /**
     * Sets the user-defined fields for feeds.
     *
     * @param userFields the new set of user-defined fields
     */
    void setUserFields(@Nonnull Set<UserField> userFields);
}
