package com.thinkbiganalytics.metadata.api.category;

import com.thinkbiganalytics.metadata.api.MissingUserPropertyException;
import com.thinkbiganalytics.metadata.api.feed.Feed;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * A category is a collection of zero or more feeds.
 */
public interface Category {

    interface ID extends Serializable {}

    ID getId();

    List<? extends Feed> getFeeds();

    String getDisplayName();

    String getName();

    Integer getVersion();

    String getDescription();

    void setDescription(String description);

    DateTime getCreatedTime();

    DateTime getModifiedTime();

    void setDisplayName(String displayName);

    void setName(String name);

    void setCreatedTime(DateTime createdTime);

    void setModifiedTime(DateTime modifiedTime);

    /**
     * Gets the user-defined properties for this category.
     *
     * @return the user-defined properties
     * @since 0.3.0
     */
    @Nonnull
    Map<String, String> getUserProperties();

    /**
     * Replaces the user-defined properties for this category with the specified properties.
     *
     * @param userProperties the new user-defined properties
     * @throws MissingUserPropertyException if a required property is empty or missing
     * @since 0.3.0
     */
    void setUserProperties(@Nonnull final Map<String, String> userProperties);
}
