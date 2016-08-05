package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.api.MissingUserPropertyException;
import com.thinkbiganalytics.metadata.api.Propertied;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * A feed is a specification for how data should flow into and out of a system.
 *
 * @param <C> the type of parent category
 */
public interface Feed<C extends Category> extends Propertied, Serializable {

    interface ID extends Serializable { }

    enum State {ENABLED, DISABLED, DELETED }

    ID getId();

    String getName();

    String getQualifiedName();

    String getDisplayName();

    String getDescription();

    State getState();

    boolean isInitialized();

    FeedPrecondition getPrecondition();

    List<Feed<C>> getDependentFeeds();

    boolean addDependentFeed(Feed<?> feed);

    boolean removeDependentFeed(Feed<?> feed);

    void setInitialized(boolean flag);

    void setDisplayName(String name);

    void setDescription(String descr);

    void setState(State state);

    C getCategory();

    String getVersionName();

    DateTime getCreatedTime();

    DateTime getModifiedTime();

    List<? extends ServiceLevelAgreement> getServiceLevelAgreements();

    /**
     * Gets the user-defined properties for this feed.
     *
     * @return the user-defined properties
     * @since 0.3.0
     */
    @Nonnull
    Map<String, String> getUserProperties();

    /**
     * Replaces the user-defined properties for this feed with the specified properties.
     *
     * @param userProperties the new user-defined properties
     * @throws MissingUserPropertyException if a required property is empty or missing
     * @since 0.3.0
     */
    void setUserProperties(@Nonnull Map<String, String> userProperties);

    // -==-=-=-=- Deprecated -=-=-=-=-=-

    @Deprecated
    List<? extends FeedSource> getSources();

    @Deprecated
    FeedSource getSource(Datasource.ID id);

    @Deprecated
    List<? extends FeedDestination> getDestinations();

    @Deprecated
    FeedDestination getDestination(Datasource.ID id);
}
