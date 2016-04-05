/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Sean Felten
 */
public class BaseId implements Serializable {
    
    private static final long serialVersionUID = 7625329514504205283L;
    
    private UUID uuid;
    
    public BaseId() {
        super();
    }

    public BaseId(Serializable ser) {
        if (ser instanceof String) {
            this.uuid = UUID.fromString((String) ser);
        } else if (ser instanceof UUID) {
            this.uuid = (UUID) ser;
        } else {
            throw new IllegalArgumentException("Unknown ID value: " + ser);
        }
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (getClass().isAssignableFrom(obj.getClass())) {
            BaseId that = (BaseId) obj;
            return Objects.equals(this.uuid, that.uuid);
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getClass(), this.uuid);
    }
    
    @Override
    public String toString() {
        return this.uuid.toString();
    }
}
