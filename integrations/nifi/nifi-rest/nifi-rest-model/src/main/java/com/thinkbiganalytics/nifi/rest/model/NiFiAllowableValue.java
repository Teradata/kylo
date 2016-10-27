package com.thinkbiganalytics.nifi.rest.model;

import java.util.Objects;

/**
 * The allowable values for a property with a constrained set of options.
 */
public class NiFiAllowableValue {

    private String displayName;
    private String value;
    private String description;

    /**
     * @return the human-readable value that is allowed for this PropertyDescriptor
     */
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the value for this allowable value
     */
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return a description of this Allowable Value, or {@code null} if no description is given
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NiFiAllowableValue) {
            return Objects.equals(value, ((NiFiAllowableValue)obj).value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
