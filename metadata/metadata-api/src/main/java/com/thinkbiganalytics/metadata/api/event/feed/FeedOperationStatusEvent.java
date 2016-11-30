/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event.feed;

import com.thinkbiganalytics.metadata.api.event.AbstractMetadataEvent;

/**
 *
 * @author Sean Felten
 */
public class FeedOperationStatusEvent extends AbstractMetadataEvent<OperationStatus> {

    private static final long serialVersionUID = 1L;
    
    public FeedOperationStatusEvent(OperationStatus status) {
        super(status);
    }
}
