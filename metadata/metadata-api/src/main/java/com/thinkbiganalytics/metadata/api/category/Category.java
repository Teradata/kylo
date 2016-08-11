package com.thinkbiganalytics.metadata.api.category;

import com.thinkbiganalytics.metadata.api.MissingUserPropertyException;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.security.AccessControlled;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A category is a collection of zero or more feeds.
 */
public interface Category extends AccessControlled {

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
     * <p>If the user-defined field descriptors are given then a check is made to ensure that all required properties are specified.</p>
     *
     * @param userProperties the new user-defined properties
     * @param userFields the user-defined field descriptors
     * @throws MissingUserPropertyException if a required property is empty or missing
     * @see CategoryProvider#getUserFields() for the user-defined field descriptors
     * @since 0.4.0
     */
    void setUserProperties(@Nonnull Map<String, String> userProperties, @Nonnull Set<UserFieldDescriptor> userFields);
}
