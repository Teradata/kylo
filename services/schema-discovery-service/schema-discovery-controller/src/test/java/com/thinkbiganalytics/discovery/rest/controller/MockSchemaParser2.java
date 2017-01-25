package com.thinkbiganalytics.discovery.rest.controller;

/*-
 * #%L
 * thinkbig-schema-discovery-controller
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

import com.thinkbiganalytics.discovery.model.DefaultHiveSchema;
import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;
import com.thinkbiganalytics.policy.PolicyProperty;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@SchemaParser(name = "Test2", description = "Test parser 2", tags = {"XML", "JSON"}, clientHelper = "Test2Helper")
public class MockSchemaParser2 implements FileSchemaParser {

    @PolicyProperty(name = "Auto Detect?", hint = "Auto detect will attempt to infer delimiter from the sample file.", value = "true")
    private boolean autoDetect = true;

    @PolicyProperty(name = "Header?", hint = "Whether file has a header.", value = "true")
    private boolean headerRow = true;

    @Override
    public Schema parse(InputStream is, Charset charset, TableSchemaType target) throws IOException {
        return new DefaultHiveSchema();
    }
}
