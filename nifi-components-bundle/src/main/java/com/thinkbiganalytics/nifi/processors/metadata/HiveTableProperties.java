/**
 * 
 */
package com.thinkbiganalytics.nifi.processors.metadata;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.util.StandardValidators;

/**
 *
 * @author Sean Felten
 */
public interface HiveTableProperties {
    static final String DATABASE_NAME_PROP = "database.name";
    static final String TABLE_NAME_PROP = "table.name";
    static final String TABLE_LOCATION_PROP = "table.location";
    static final String TABLE_PARTITIONS_PROP = "table.partitions";
    
    static final PropertyDescriptor DATABASE_NAME = new PropertyDescriptor.Builder()
            .name(DATABASE_NAME_PROP)
            .displayName("Database name")
            .description("The name of the database containing the table where the feed writes its processing results")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    static final PropertyDescriptor TABLE_NAME = new PropertyDescriptor.Builder()
            .name(TABLE_NAME_PROP)
            .displayName("Table name")
            .description("The name of the table where the feed writes its processing results")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    static final PropertyDescriptor TABLE_LOCATION = new PropertyDescriptor.Builder()
            .name(TABLE_LOCATION_PROP)
            .displayName("Table location")
            .description("The URI specifying the location where the feed writes its processing results")
            .required(false)
            .addValidator(StandardValidators.URI_VALIDATOR)
            .build();

}
