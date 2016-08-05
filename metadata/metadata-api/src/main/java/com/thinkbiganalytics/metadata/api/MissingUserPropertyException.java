package com.thinkbiganalytics.metadata.api;

import javax.annotation.Nonnull;

/**
 * Thrown to indicate that a require user-defined property is missing a value.
 */
public class MissingUserPropertyException extends RuntimeException {

    private static final long serialVersionUID = -8809671023051214040L;

    /** Property this is missing a value */
    @Nonnull
    private String propertyName;

    /**
     * Constructs a {@code MissingUserPropertyException} for the specified property.
     *
     * @param propertyName the property that is missing a value
     */
    public MissingUserPropertyException(@Nonnull final String propertyName) {
        super("A required property is missing a value: " + propertyName);
        this.propertyName = propertyName;
    }

    /**
     * Gets the property that is missing a value.
     *
     * @return the name of the property
     */
    @Nonnull
    public String getPropertyName() {
        return propertyName;
    }
}
