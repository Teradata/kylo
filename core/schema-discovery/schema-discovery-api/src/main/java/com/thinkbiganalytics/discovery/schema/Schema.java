package com.thinkbiganalytics.discovery.schema;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Schema represents the structure and encoding of a dataset. Given a embedded schema such as cobol copybook, avro the schema only derives information required to extract field structure and
 * properties of that schema that might be considered useful metadata.
 */
public interface Schema {

    /**
     * Returns the unique id of the schema object
     */
    UUID getID();

    /**
     * Returns the unique name
     */
    String getName();

    /**
     * Sets the unique name
     */
    void setName(String name);

    /**
     * Business description of the object
     */
    String getDescription();

    /**
     * Return the canonical charset name
     */
    String getCharset();

    /**
     * Return format-specific properties of the data structure. For example, whether the file contains a header, footer, field or row delimiter types, escape characters, etc.
     */
    Map<String, String> getProperties();

    /**
     * Returns the field structure
     */
    List<Field> getFields();

}
