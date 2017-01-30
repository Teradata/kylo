/**
 *
 */
package com.thinkbiganalytics.nifi.v2.metadata;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.util.StandardValidators;

/**
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
