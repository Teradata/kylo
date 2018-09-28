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

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Parser for generic text or binary data.
 * NOTE TODO binary script doesnt work yet.
 */
@SchemaParser(name = "Text", allowSkipHeader = false, description = "read generice text file resulting in a single column output of text", tags = {"TEXT"}, usesSpark = true, primary = true, mimeTypes = "text", sparkFormat = "text")
public class TextBinarySparkFileSchemaParser extends AbstractSparkFileSchemaParser implements FileSchemaParser {

   // @PolicyProperty(name = "Binary",displayName = "Binary data?", hint = "Set to true if this is binary data", type = PolicyPropertyTypes.PROPERTY_TYPE.select, selectableValues = {"true", "false"})
    private boolean binary = false;

    @Override
    public SparkFileType getSparkFileType() {
        return SparkFileType.TEXT;
    }


    private class TextFileSparkCommandBuilder extends AbstractSparkCommandBuilder {

        public TextFileSparkCommandBuilder(String dataframeVariable, Integer limit) {
            super(dataframeVariable, limit);
        }


        @Override
        public String build(String pathToFile) {
            StringBuilder sb = new StringBuilder();
            sb.append((dataframeVariable != null ? "var " + dataframeVariable + " = " : ""));
            if(binary) {
                sb.append(String.format(" sc.binaryFiles(\"%s\").toDF()",pathToFile));
            }
            else {
                sb.append((dataframeVariable != null ? "var " + dataframeVariable + " = " : ""));
                sb.append(" sqlContext.read.format(\"text\")");
                sb.append(String.format(".load(\"%s\")", pathToFile));
            }
            if (isLimit()) {
                sb.append(String.format(".limit(%s)", limit));
            }
            return sb.toString();
        }


        @Override
        public String build(List<String> paths) {
            String path = StringUtils.join(paths, ",");
            return build(path);
        }

    }

    @Override
    public SparkCommandBuilder getSparkCommandBuilder() {
        return new TextFileSparkCommandBuilder(dataFrameVariable, limit);
    }


}
