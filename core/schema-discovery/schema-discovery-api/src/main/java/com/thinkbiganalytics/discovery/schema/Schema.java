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
