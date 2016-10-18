package com.thinkbiganalytics.metadata.api.feed;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.MissingUserPropertyException;
import com.thinkbiganalytics.metadata.api.Propertied;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.security.AccessControlled;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 * A feed is a specification for how data should flow into and out of a system.
 *
 * @param <C> the type of parent category
 */
public interface Feed<C extends Category> extends Propertied, AccessControlled, Serializable {

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
     * <p>If the user-defined field descriptors are given then a check is made to ensure that all required properties are specified. These field descriptors should be the union of
     * {@link FeedProvider#getUserFields()} and {@link com.thinkbiganalytics.metadata.api.category.CategoryProvider#getFeedUserFields(Category.ID)} with precedence given to the first.</p>
     *
     * @param userProperties the new user-defined properties
     * @param userFields the user-defined fields
     * @throws MissingUserPropertyException if a required property is empty or missing
     * @see FeedProvider#getUserFields() for the user-defined field descriptors for all feeds
     * @see com.thinkbiganalytics.metadata.api.category.CategoryProvider#getFeedUserFields(Category.ID) for the user-defined field descriptors for all feeds within a given category
     * @since 0.4.0
     */
    void setUserProperties(@Nonnull Map<String, String> userProperties, @Nonnull Set<UserFieldDescriptor> userFields);


    List<? extends FeedSource> getSources();

    FeedSource getSource(Datasource.ID id);

    List<? extends FeedDestination> getDestinations();

    FeedDestination getDestination(Datasource.ID id);

    List<? extends HadoopSecurityGroup> getSecurityGroups();

    void setSecurityGroups(List<? extends HadoopSecurityGroup> securityGroups);

    /**
     * @param waterMarkName the name of the high water mark
     * @return an optional string value of the high water mark
     */
    Optional<String> getWaterMarkValue(String waterMarkName);
    
    /**
     * @return the set of existing high water mark names
     */
    Set<String> getWaterMarkNames();
    
    /**
     * @param waterMarkName the name of the high water mark
     * @param value the current value of the water mark
     */
    void setWaterMarkValue(String waterMarkName, String value);
}
