/**
 * 
 */
package com.thinkbiganalytics.metadata.event;

import java.io.Serializable;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.event.MetadataEvent;

/**
 *
 * @author Sean Felten
 */
public class BaseMetadataEvent<C extends Serializable> implements MetadataEvent<C> {
    
    private static final long serialVersionUID = 1L;
    
    private final DateTime timestamp;
    private final C data;
    
    public BaseMetadataEvent(C data) {
        this(data, DateTime.now());
    }
    
    public BaseMetadataEvent(C data, DateTime time) {
        this.timestamp = time;
        this.data = data;
    }

    @Override
    public DateTime getTimestamp() {
        return this.timestamp;
    }

    @Override
    public C getData() {
        return this.data;
    }

}
