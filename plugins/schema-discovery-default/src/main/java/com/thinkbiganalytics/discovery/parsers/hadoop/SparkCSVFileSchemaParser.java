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
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * CSV parser using Spark
 * Spark 2:
 *   - Native support for CSV parsing
 * Spark 1:
 *   https://github.com/databricks/spark-csv
 *   kylo spark.properties needs to have this --package property added:
 *      Spark compiled with Scala 2.11
 *        --packages com.databricks:spark-csv_2.11:1.5.0
 *      Spark compiled with Scala 2.10
 *        --packages com.databricks:spark-csv_2.10:1.5.0
 */
@SchemaParser(name = "CSV", allowSkipHeader = true, description = "Supports CSV formatted files.", tags = {"CSV"}, usesSpark = true, primary = false)
public class SparkCSVFileSchemaParser extends AbstractSparkFileSchemaParser implements FileSchemaParser {

    @PolicyProperty(name = "Auto Detect?", hint = "Auto detect will attempt to infer delimiter from the sample file.", type = PolicyPropertyTypes.PROPERTY_TYPE.select,
                    selectableValues = {"true", "false"})
    private boolean autoDetect = true;

    @PolicyProperty(name = "Header?", hint = "Whether file has a header.", value = "true", type = PolicyPropertyTypes.PROPERTY_TYPE.select, selectableValues = {"true", "false"})
    private boolean headerRow = true;

    @PolicyProperty(name = "Delimiter Char", hint = "Character separating fields", value = ",")
    private String separatorChar = ",";

    @PolicyProperty(name = "Quote Char", hint = "Character enclosing a quoted string", value = "\"")
    private String quoteChar = "\"";

    @PolicyProperty(name = "Escape Char", hint = "Escape character", value = "\\")
    private String escapeChar = "\\";



    @Override
    public SparkFileType getSparkFileType() {
        return SparkFileType.CSV;
    }


    private class CsvSparkCommandBuilder extends AbstractSparkCommandBuilder {



        private Map<String, String> options = new HashMap();

        public CsvSparkCommandBuilder(String dataframeVariable, Integer limit) {
            super(dataframeVariable, limit);
        }

        private void addOption(StringBuilder sb, String option, String val) {
            sb.append(String.format(".option(\"%s\",\"%s\")", option, val));
        }

        @Override
        public String build(String pathToFile) {
            StringBuilder sb = new StringBuilder();
            appendDataFrameVariable(sb);
            sb.append("sqlContext.read.format(\"csv\")");
            addOptions(sb);
            sb.append(String.format(".load(\"%s\")", pathToFile));
            if (isLimit()) {
                sb.append(String.format(".limit(%s)", limit));
            }
            return sb.toString();
        }

        private void addOptions(StringBuilder sb){
            addOption(sb, "header", headerRow + "");
            addOption(sb, "inferSchema", autoDetect + "");
            addOption(sb,"delimiter",separatorChar);
            if (escapeChar.equalsIgnoreCase("\\")) {
                escapeChar = "\\\\";
            }
            addOption(sb, "escape", escapeChar);
            if (quoteChar.equalsIgnoreCase("\"")) {
                quoteChar = "\\\"";
            }
            addOption(sb, "quote", quoteChar);
        }
    }

    @Override
    public SparkCommandBuilder getSparkCommandBuilder() {
        return new CsvSparkCommandBuilder(dataFrameVariable, limit);
    }


}
