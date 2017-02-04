package com.thinkbiganalytics.discovery.schema;

/*-
 * #%L
 * thinkbig-schema-discovery-api
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

/**
 * Represents a Hive table
 */
public interface HiveTableSchema extends TableSchema {

    /**
     * Returns the storage format of the data such as the serde
     *
     * @return hive storage format
     */
    String getHiveFormat();

    /**
     * Set the storage format of the data such as the serde
     *
     * @param hiveFormat hive storage format
     */
    void setHiveFormat(String hiveFormat);

    /**
     * Whether the data represents a binary or structured format like AVRO, ORC
     *
     * @return true/false indicating if data is in a structured format
     */
    Boolean isStructured();

    /**
     * Whether the data represents a binary format or structured format like AVRO, ORC
     *
     * @param binary indication if data is in a structured format
     */
    void setStructured(boolean binary);


}
