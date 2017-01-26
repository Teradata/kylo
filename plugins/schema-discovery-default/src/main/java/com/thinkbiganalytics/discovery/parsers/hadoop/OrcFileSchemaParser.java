package com.thinkbiganalytics.discovery.parsers.hadoop;

/*-
 * #%L
 * thinkbig-schema-discovery-default
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

import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@SchemaParser(name = "ORC", description = "Supports ORC formatted files.", tags = {"ORC"})
public class OrcFileSchemaParser extends AbstractSparkFileSchemaParser implements FileSchemaParser {

    @Override
    public Schema parse(InputStream is, Charset charset, TableSchemaType target) throws IOException {
        return getSparkParserService().doParse(is, SparkFileSchemaParserService.SparkFileType.ORC, target);
    }
}
