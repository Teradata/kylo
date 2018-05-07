package com.thinkbiganalytics.discovery.parsers.hadoop;

/*-
 * #%L
 * XMLFileSchemaParser.java
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
import com.thinkbiganalytics.discovery.parsers.csv.CSVFileSchemaParser;
import com.thinkbiganalytics.discovery.schema.HiveTableSchema;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

//@SchemaParser(name = "XML", allowSkipHeader = false, description = "Supports XML formatted files.", tags = {"XML"})
public class XMLFileSchemaParser extends AbstractSparkFileSchemaParser implements FileSchemaParser {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CSVFileSchemaParser.class);

    @PolicyProperty(name = "Row Tag", required = true, hint = "Specify root tag to extract from", value = ",")
    private String rowTag = "";

    @Override
    public Schema parse(InputStream is, Charset charset, TableSchemaType target) throws IOException {
        HiveTableSchema schema = (HiveTableSchema) getSparkParserService().doParse(is, SparkFileSchemaParserService.SparkFileType.XML, target, new XMLCommandBuilder(rowTag));
        schema.setStructured(true);
        return schema;
    }

    static class XMLCommandBuilder implements SparkCommandBuilder {

        String xmlRowTag;

        XMLCommandBuilder(String rowTag) {
            this.xmlRowTag = rowTag;
        }

        @Override
        public String build(String pathToFile) {
            StringBuffer sb = new StringBuffer();

            sb.append("import com.databricks.spark.xml._;\n");
            sb.append(String.format("sqlContext.read.format(\"com.databricks.spark.xml\").option(\"rowTag\",\"%s\").load(\"%s\")", xmlRowTag, pathToFile));
            return sb.toString();
        }
    }

    public String getRowTag() {
        return rowTag;
    }

    public void setRowTag(String rowTag) {
        this.rowTag = rowTag;
    }

    public static void main(String[] args) {

        XMLCommandBuilder builder = new XMLCommandBuilder("tagName");
        System.out.println(builder.build("/file.xml"));
    }

}
