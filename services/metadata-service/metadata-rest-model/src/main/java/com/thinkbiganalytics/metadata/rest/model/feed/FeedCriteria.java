/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.feed;


/**
 *
 * @author Sean Felten
 */
public interface FeedCriteria {

    static final String CATEGORY = "cat";
    static final String NAME = "name";
    static final String SRC_ID = "srcid";
    static final String DEST_ID = "destid";

    FeedCriteria sourceDatasource(String dsId);
    FeedCriteria destinationDatasource(String dsId);
    FeedCriteria name(String name);
    FeedCriteria category(String name);
}
