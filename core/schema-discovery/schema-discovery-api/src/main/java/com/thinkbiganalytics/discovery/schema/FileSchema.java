package com.thinkbiganalytics.discovery.schema;

/**
 * Schema of a file
 */
public interface FileSchema extends Schema {

    /**
     * Retrieve canonical format name such as CSV, XML, JPG, etc.
     */
    String getFormat();

    /**
     * Whether the file is a binary
     */
    boolean isBinary();

    /**
     * Whether the file contains embedded schema such as AVRO, XSD, etc.
     */
    boolean hasEmbeddedSchema();


}
