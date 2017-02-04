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
 * Schema of a file
 */
public interface FileSchema extends Schema {

    /**
     * Retrieve canonical format name such as CSV, XML, JPG, etc.
     *
     * @return canonical format string
     */
    String getFormat();

    /**
     * Whether the file is a binary
     *
     * @return true/false indicating if file is binary type
     */
    boolean isBinary();

    /**
     * Whether the file contains embedded schema such as AVRO, XSD, etc.
     *
     * @return true/false indicating if file has schema included within it
     */
    boolean hasEmbeddedSchema();


}
