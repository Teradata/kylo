/**
 *
 */
package com.thinkbiganalytics.metadata.core;

/*-
 * #%L
 * thinkbig-metadata-core
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


import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 *
 */
public abstract class BaseId implements Serializable {

    private static final long serialVersionUID = 7625329514504205283L;

    public BaseId() {
        super();
    }

    public BaseId(Serializable ser) {
        if (ser instanceof String) {
            String uuid = (String) ser;
            if (!StringUtils.contains(uuid, "-")) {
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
