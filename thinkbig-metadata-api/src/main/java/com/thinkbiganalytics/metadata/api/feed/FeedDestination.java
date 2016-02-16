/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import java.io.Serializable;

/**
 *
 * @author Sean Felten
 */
public interface FeedDestination extends FeedData {

    interface ID extends Serializable { }

    ID getId();
}
