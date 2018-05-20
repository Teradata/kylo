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

import com.thinkbiganalytics.discovery.parsers.hadoop.SparkCommandBuilder;
import com.thinkbiganalytics.discovery.parsers.hadoop.SparkFileType;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parses a file using Spark to determine its structure and format.
 */
public interface SparkFileSchemaParser extends FileSchemaParser {
    enum SparkVersion{
        SPARK1("v1"),SPARK2("v2");

        String version;
        SparkVersion(String ver){
            this.version = ver;
        }

       public String getVersion(){
            return this.version;
        }
    }

    public static Integer NO_LIMIT = -1;

    void setLimit(Integer limit);

    void setDataFrameVariable(String dataFrameVariable);

    public SampleFileSparkScript getSparkScript(InputStream isa) throws IOException;

    public SparkFileType getSparkFileType();

    /**
     * Return the command builder used to parse the sample file to detect schema
     */
    public SparkCommandBuilder getSparkSchemaDetectionCommandBuilder();

    /**
     * return the command builder used to generate the spark script
     * this is used in the Data Wrangler to transform a local file
     */
    public SparkCommandBuilder getSparkScriptCommandBuilder();

    void setSparkVersion(SparkVersion sparkVersion);

    SparkVersion getSparkVersion();

}
