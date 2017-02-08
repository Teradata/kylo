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
 * A user-defined field (or business metadata) on categories or feeds.
 *
 * <p>These field are pre-defined by an administrator. A field must have a {@code systemName} and a {@code value}. All other attributes are optional.</p>
 *
 * @see UserProperty
 * @since 0.4.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserField {

    /**
     * A human-readable specification
     */
    private String description;

    /**
     * A human-readable title
     */
    private String displayName;

    /**
     * Index for the display order from 0 and up
     */
    private Integer order;

    /**
     * Indicates that the value cannot be empty
     */
    private Boolean required;

    /**
     * An internal identifier
     */
    private String systemName;

    /**
     * Gets a human-readable specification for this property.
     *
     * @return a human-readable specification
     * @see #setDescription(String)
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets a human-readable specification for this property.
     *
     * @param description a human-readable specification
     * @see #getDescription()
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets a human-readable title for this property.
     *
     * <p>This should be displayed instead of the {@code systemName} if it is non-null.</p>
     *
     * @return a human-readable title
     * @see #setDisplayName(String)
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets a human-readable title for this property.
     *
     * @param displayName a human-readable title
     * @see #getDisplayName()
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the index of the display order for this property.
     *
     * <p>The first property will have a value of 0, with later properties increasing in value.</p>
     *
     * @return the display order index
     * @see #setOrder(Integer)
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * Sets the index of the display order for this property.
     *
     * @param order the dispaly order index
     * @see #getOrder()
     */
    public void setOrder(Integer order) {
        this.order = order;
    }

    /**
     * Indicates that the {@code value} cannot be empty.
     *
     * @return {@code true} if the {@code value} cannot be empty; {@code false} or {@code null} otherwise
     * @see #setRequired(Boolean)
     */
    public Boolean isRequired() {
        return required;
    }

    /**
     * Set to indicate if the {@code value} can or cannot be empty.
     *
     * @param required {@code true} if the {@code value} cannot be empty; {@code false} or {@code null} if it can be empty
     * @see #isRequired()
     */
    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     * Sets the internal identifier for this property.
     *
     * @return the internal identifier
     * @see #setSystemName(String)
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * Gets the internal identifier for this property.
     *
     * @param systemName the internal identifier
     * @see #getSystemName()
     */
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(systemName);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", systemName)
            .toString();
    }
}
