/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.feed.Feed.ID;

/**
 *
 * @author Sean Felten
 */
public class BaseFeed implements Feed {

    private ID Id;
    private String Name;
    private String Description;
    private FeedSource Source;
    private FeedDestination destination;

    public ID getId() {
        return Id;
    }

    public String getName() {
        return Name;
    }

    public String getDescription() {
        return Description;
    }

    public FeedSource getSource() {
        return Source;
    }

    public FeedDestination getDestination() {
        return destination;
    }

}
