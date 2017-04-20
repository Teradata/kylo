/**
 *
 */
package com.thinkbiganalytics.jpa;

/*-
 * #%L
 * thinkbig-commons-jpa
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
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
        if (this == obj) return true;
        if (obj == null) return false;
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
