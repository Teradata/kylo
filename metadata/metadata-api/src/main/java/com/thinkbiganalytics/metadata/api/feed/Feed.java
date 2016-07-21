/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.api.Propertied;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Sean Felten
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

    
    // -==-=-=-=- Deprecated -=-=-=-=-=-
    
    List<? extends FeedSource> getSources();
    
    FeedSource getSource(Datasource.ID id);
//    
//    FeedSource getSource(FeedSource.ID id);

    List<? extends FeedDestination> getDestinations();

    FeedDestination getDestination(Datasource.ID id);
//
//    FeedDestination getDestination(FeedDestination.ID id);
}
