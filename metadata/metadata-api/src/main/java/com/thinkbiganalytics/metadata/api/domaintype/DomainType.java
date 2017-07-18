package com.thinkbiganalytics.metadata.api.domaintype;

import java.io.Serializable;

/**
 * Defines the domain type (zip, phone, credit card) of a column.
 */
public interface DomainType extends Serializable {

    /**
     * Gets the unique identifier.
     */
    ID getId();

    /**
     * Gets a human-readable description.
     */
    String getDescription();

    /**
     * Sets a human-readable description.
     */
    void setDescription(String value);

    /**
     * Gets the field policy as a JSON document.
     */
    String getFieldPolicyJson();

    /**
     * Sets the field policy as a JSON document.
     */
    void setFieldPolicyJson(String value);

    /**
     * Gets the name of the icon.
     */
    String getIcon();

    /**
     * Sets the name of the icon.
     */
    void setIcon(String value);

    /**
     * Gets the icon color.
     */
    String getIconColor();

    /**
     * Sets the icon color.
     */
    void setIconColor(String value);

    /**
     * Gets the regular expression for matching sample data.
     */
    String getRegex();

    /**
     * Sets the regular expression for matching sample data.
     */
    void setRegex(String value);

    /**
     * Gets a human-readable title.
     */
    String getTitle();

    /**
     * Sets a human-readable title.
     */
    void setTitle(String value);

    /**
     * A unique identifier for a domain type.
     */
    interface ID extends Serializable {

    }
}
