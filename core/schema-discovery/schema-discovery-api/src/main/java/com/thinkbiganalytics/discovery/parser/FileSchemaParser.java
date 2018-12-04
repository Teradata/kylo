package com.thinkbiganalytics.discovery.parser;

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

import com.thinkbiganalytics.discovery.schema.HiveTableSchema;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.schema.TableSettings;
import com.thinkbiganalytics.discovery.util.TableSchemaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * Parses a file to determine its structure and format.
 */
public interface FileSchemaParser extends Serializable {

    /**
     * Parse the provided file and builds a schema with data types that fit the target
     *
     * @param is      the raw data to parse
     * @param charset the character-set (e.g. UTF-8)
     * @param target  the target platform (e.g. Hive)
     * @return a derived schema for the source
     */
    Schema parse(InputStream is, Charset charset, TableSchemaType target) throws IOException;

    TableSettings parseTableSettings(InputStream is, Charset charset, TableSchemaType target) throws IOException;

    /**
     * derive the table settings from the type
     * @param target
     * @return
     * @throws IOException
     */
    TableSettings deriveTableSettings(TableSchemaType target) throws IOException;

    /**
     * Do we need to open the file to set the table settings?
     * @return
     */
    boolean tableSettingsRequireFileInspection();
}
