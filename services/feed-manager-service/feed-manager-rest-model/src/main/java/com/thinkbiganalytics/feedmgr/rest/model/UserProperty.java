package com.thinkbiganalytics.feedmgr.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;

import java.util.Objects;

/**
 * A user-defined property (or business metadata) on a category or feed.
 *
 * <p>These properties may be pre-defined by an administrator or added to a specific entity by a user. A property must have a {@code systemName} and a {@code value}. All other fields are optional.</p>
 *
 * @since 0.3.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProperty {

    /** A human-readable specification */
    private String description;

    /** A human-readable title */
    private String displayName;

    /** Indicates that only the value may be changed */
    private Boolean locked;

    /** Index for the display order from 0 and up */
    private Integer order;

    /** Indicates that the value cannot be empty */
    private Boolean required;

    /** An internal identifier */
    private String systemName;

    /** The value assigned to the property */
    private String value;

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
     * Indicates that only the {@code value} may be changed.
     *
     * @return {@code true} if only the {@code value} may be changed; {@code false} or {@code null} otherwise
     * @see #setLocked(Boolean)
     */
    public Boolean getLocked() {
        return locked;
    }

    /**
     * Set to indicate if all members or only the {@code value} may be changed.
     *
     * @param locked {@code true} if only the {@code value} may be changed; {@code false} or {@code null} if any member may be changed
     * @see #getLocked()
     */
    public void setLocked(Boolean locked) {
        this.locked = locked;
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
    public Boolean getRequired() {
        return required;
    }

    /**
     * Set to indicate if the {@code value} can or cannot be empty.
     *
     * @param required {@code true} if the {@code value} cannot be empty; {@code false} or {@code null} if it can be empty
     * @see #getRequired()
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
        return Objects.hashCode(systemName);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", systemName)
                .add("value", value)
                .toString();
    }
}
