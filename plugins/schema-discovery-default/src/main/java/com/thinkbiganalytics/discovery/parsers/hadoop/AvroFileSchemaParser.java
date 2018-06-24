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

import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.discovery.parser.SparkFileSchemaParser;

import java.util.List;

@SchemaParser(name = "Avro", description = "Supports Avro formatted files.", tags = {"Avro"}, usesSpark = true, mimeTypes = "application/avro",sparkFormat = "avro")
public class AvroFileSchemaParser extends AbstractSparkFileSchemaParser implements SparkFileSchemaParser {

    static class AvroCommandBuilder extends AbstractSparkCommandBuilder {

        @Override
        public String build(String pathToFile) {
            StringBuilder sb = new StringBuilder();
            sb.append("import com.databricks.spark.avro._\n");
            sb.append("sqlContext.sparkContext.hadoopConfiguration.set(\"avro.mapred.ignore.inputs.without.extension\", \"false\")\n");
            sb.append((dataframeVariable != null ? "var " + dataframeVariable + " = " : ""));
            sb.append(String.format("sqlContext.read.%s(\"%s\")", "avro", pathToFile));
            if(isLimit()) {
                sb.append(String.format(".limit(%s)", getLimit()));
            }
            sb.append(".toDF()");
            return sb.toString();
        }

        @Override
        public String build(List<String> paths) {
            StringBuilder sb = new StringBuilder();
            sb.append("import com.databricks.spark.avro._\n");
            sb.append("sqlContext.sparkContext.hadoopConfiguration.set(\"avro.mapred.ignore.inputs.without.extension\", \"false\")\n");

            sb.append(unionDataFrames(paths,"sqlContext.read.%s(\"%s\").toDF()\n","avro"));

            return sb.toString();
        }
    }

    @Override
    public SparkFileType getSparkFileType() {
        return SparkFileType.AVRO;
    }


    @Override
    public SparkCommandBuilder getSparkCommandBuilder() {
        AvroCommandBuilder avroCommandBuilder = new AvroCommandBuilder();
        avroCommandBuilder.setLimit(limit);
        avroCommandBuilder.setDataframeVariable(dataFrameVariable);
        return avroCommandBuilder;
    }
}
