/**
 *
 */
package com.thinkbiganalytics.nifi.v2.metadata;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.util.StandardValidators;

/**
 * @author Sean Felten
 */
public interface HiveTableProperties {
    String DATABASE_NAME_PROP = "database.name";
    String TABLE_NAME_PROP = "table.name";
    String TABLE_LOCATION_PROP = "table.location";
    String TABLE_PARTITIONS_PROP = "table.partitions";

    PropertyDescriptor DATABASE_NAME = new PropertyDescriptor.Builder()
            .name(DATABASE_NAME_PROP)
            .displayName("Database name")
            .description("The name of the database containing the table where the feed writes its processing results")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    PropertyDescriptor TABLE_NAME = new PropertyDescriptor.Builder()
            .name(TABLE_NAME_PROP)
            .displayName("Table name")
            .description("The name of the table where the feed writes its processing results")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    PropertyDescriptor TABLE_LOCATION = new PropertyDescriptor.Builder()
            .name(TABLE_LOCATION_PROP)
            .displayName("Table location")
            .description("The URI specifying the location where the feed writes its processing results")
            .required(false)
            .addValidator(StandardValidators.URI_VALIDATOR)
            .build();

}
