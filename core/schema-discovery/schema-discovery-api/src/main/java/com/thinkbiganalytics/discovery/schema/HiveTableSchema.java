package com.thinkbiganalytics.discovery.schema;

/**
 * Represents a Hive table
 */
public interface HiveTableSchema extends TableSchema {

    /**
     * Returns the storage format of the data such as the serde
     */
    String getHiveFormat();

    /**
     * Set the storage format of the data such as the serde
     */
    void setHiveFormat(String hiveFormat);

    /**
     * Whether the data represents a binary or structured format like AVRO, ORC
     */
    Boolean isStructured();

    /**
     * Whether the data represents a binary format or structured format like AVRO, ORC
     */
    void setStructured(boolean binary);



}
