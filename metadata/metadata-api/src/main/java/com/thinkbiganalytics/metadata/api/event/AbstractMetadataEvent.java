/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import java.io.Serializable;
import java.security.Principal;

import org.joda.time.DateTime;

/**
 *
 * @author Sean Felten
 */
public abstract class AbstractMetadataEvent<C extends Serializable> implements MetadataEvent<C> {
    
    private static final long serialVersionUID = 1L;
    
    private final DateTime timestamp;
    private final C data;
    private final Principal userPrincipal;
    
    public AbstractMetadataEvent(C data) {
        this(data, DateTime.now(), null);
    }
    
    public AbstractMetadataEvent(C data, Principal user) {
        this(data, DateTime.now(), user);
    }
    
    public AbstractMetadataEvent(C data, DateTime time, Principal user) {
        this.timestamp = time;
        this.userPrincipal = user;
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

    public Principal getUserPrincipal() {
        return userPrincipal;
    }
}
