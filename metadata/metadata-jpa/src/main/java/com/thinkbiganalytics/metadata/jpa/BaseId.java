/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Sean Felten
 */
public abstract class BaseId implements Serializable {
    
    private static final long serialVersionUID = 7625329514504205283L;
    
    public BaseId() {
        super();
    }

    public BaseId(Serializable ser) {
        if (ser instanceof String) {
            setUuid(UUID.fromString((String) ser));
        } else if (ser instanceof UUID) {
            setUuid((UUID) ser);
        } else {
            throw new IllegalArgumentException("Unknown ID value: " + ser);
        }
    }
    
    public abstract UUID getUuid();
    
    public abstract void setUuid(UUID uuid);
    
    @Override
    public boolean equals(Object obj) {
        if (getClass().isAssignableFrom(obj.getClass())) {
            BaseId that = (BaseId) obj;
            return Objects.equals(getUuid(), that.getUuid());
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getClass(), getUuid());
    }
    
    @Override
    public String toString() {
        return getUuid().toString();
    }
}
