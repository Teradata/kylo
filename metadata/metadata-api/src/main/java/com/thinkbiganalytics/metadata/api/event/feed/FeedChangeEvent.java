/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event.feed;

import java.security.Principal;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.event.AbstractMetadataEvent;

/**
 *
 * @author Sean Felten
 */
public class FeedChangeEvent extends AbstractMetadataEvent<FeedChange> {

    private static final long serialVersionUID = 1L;

    public FeedChangeEvent(FeedChange data) {
        super(data);
    }

    public FeedChangeEvent(FeedChange data, Principal user) {
        super(data, user);
    }

    public FeedChangeEvent(FeedChange data, DateTime time, Principal user) {
        super(data, time, user);
    }

}
