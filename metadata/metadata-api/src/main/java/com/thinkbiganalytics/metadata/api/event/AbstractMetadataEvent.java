/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import java.io.Serializable;

import org.joda.time.DateTime;

/**
 *
 * @author Sean Felten
 */
public abstract class AbstractMetadataEvent<C extends Serializable> implements MetadataEvent<C> {
    
    private static final long serialVersionUID = 1L;
    
    private final DateTime timestamp;
    private final C data;
    
    public AbstractMetadataEvent(C data) {
        this(data, DateTime.now());
    }
    
    public AbstractMetadataEvent(C data, DateTime time) {
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
