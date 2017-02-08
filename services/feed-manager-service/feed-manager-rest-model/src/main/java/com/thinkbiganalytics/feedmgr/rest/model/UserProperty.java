package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;

import java.util.Objects;

/**
 * A user-defined field and a value for a category or feed.
 *
 * <p>A property must have a {@code systemName} and a {@code value}. All other attributes are optional.</p>
 *
 * @see UserField
 * @since 0.3.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProperty extends UserField {

    /**
     * Indicates that only the value may be changed
     */
    private Boolean locked;

    /**
     * The value assigned to the property
     */
    private String value;

    /**
     * Indicates that only the {@code value} may be changed.
     *
     * @return {@code true} if only the {@code value} may be changed; {@code false} or {@code null} otherwise
     * @see #setLocked(Boolean)
     */
    public Boolean isLocked() {
        return locked;
    }

    /**
     * Set to indicate if all members or only the {@code value} may be changed.
     *
     * @param locked {@code true} if only the {@code value} may be changed; {@code false} or {@code null} if any member may be changed
     * @see #isLocked()
     */
    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    /**
     * Gets the value assigned to this property.
     *
     * @return the value
     * @see #setValue(String)
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value for this property.
     *
     * @param value the value
     * @see #getValue()
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getSystemName());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", getSystemName())
            .add("value", value)
            .toString();
    }
}
