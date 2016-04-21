/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.client;


/**
 *
 * @author Sean Felten
 */
public interface FeedCriteria {

    FeedCriteria sourceDatasource(String dsId);
    FeedCriteria destinationDatasource(String dsId);
    FeedCriteria name(String name);
}
