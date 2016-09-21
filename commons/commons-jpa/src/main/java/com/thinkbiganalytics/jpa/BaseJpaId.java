/**
 *
 */
package com.thinkbiganalytics.jpa;


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Sean Felten
 */
public abstract class BaseJpaId implements Serializable {

    private static final long serialVersionUID = 7625329514504205283L;

    public BaseJpaId() {
        super();
    }

    public BaseJpaId(Serializable ser) {
        if (ser instanceof String) {
            String uuid = (String) ser;
            if (uuid != null && uuid.contains("-")) {
                uuid = ((String) ser).replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5");
            }
            setUuid(UUID.fromString(uuid));

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
            BaseJpaId that = (BaseJpaId) obj;
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
