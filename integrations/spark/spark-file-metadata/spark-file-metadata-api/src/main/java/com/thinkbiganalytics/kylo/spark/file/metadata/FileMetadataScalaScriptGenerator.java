package com.thinkbiganalytics.kylo.spark.file.metadata;

/*-
 * #%L
 * spark-file-metadata-api
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Arrays;
import java.util.List;

/**
 * create the scala spark script to get the File metadata
 */
public class FileMetadataScalaScriptGenerator {

    public static String getScript(String[] paths) {

        return getScript(Arrays.asList(paths));

    }

    /**
     * Return a scala script that will parse the incoming paths and return a list objects:
     *
     * Return a object:
     * files - comma separated string of file paths
     * fileList - [file1,file2]
     * fileCount - int of the files count
     * headerCount - columns in the headers of the files
     * delimiter
     * mimeType - the mimeType
     * rowTag - string the rowtag
     * properties - HashMap<string,string>
     */
    public static String getScript(List<String> paths) {

        StringBuilder sb = new StringBuilder();

        sb.append("import scala.collection.mutable.ListBuffer\n");
        sb.append("import org.apache.spark.sql.functions.{concat, lit, concat_ws,collect_list,split,size,col,when}\n");
        sb.append("import com.thinkbiganalytics.kylo.catalog._\n");
        sb.append("var listBuffer = new ListBuffer[org.apache.spark.sql.DataFrame]()\n");

        sb.append("var kyloClientBuilder = KyloCatalog.builder(sqlContext) \n");
        sb.append("var kyloClient = kyloClientBuilder.build()\n");
        sb.append("var kyloClientReader = kyloClient.read.format(\"com.thinkbiganalytics.spark.file.metadata\")\n");
        for (String path : paths) {
            sb.append("listBuffer += kyloClientReader.load(\"").append(StringEscapeUtils.escapeJava(path)).append("\")\n");
        }

        sb.append("val dataFrameList = listBuffer.toList\n");

        sb.append("var unionedFileMetadataDf : org.apache.spark.sql.DataFrame = null\n");
        sb.append("dataFrameList.foreach({ df1 =>   \n"
                  + " if(unionedFileMetadataDf == null){ \n"
                  + " unionedFileMetadataDf = df1 \n"
                  + "}\n"
                  + "else {\n"
                  + "unionedFileMetadataDf = unionedFileMetadataDf.unionAll(df1)\n"
                  + "}\n"
                  + "})\n");

        sb.append(" var fileMetadataDf = unionedFileMetadataDf.select(col(\"mimeType\"),col(\"encoding\"),"
                  + "when(col(\"properties\")(\"headerCount\").isNotNull,col(\"properties\")(\"headerCount\")).otherwise(lit(\"0\")).as(\"headerCount\")"
                  + ",when(col(\"properties\")(\"delimiter\").isNotNull,col(\"properties\")(\"delimiter\")).otherwise(lit(\"\")).as(\"delimiter\")"
                  + ",when(col(\"properties\")(\"rowTag\").isNotNull,col(\"properties\")(\"rowTag\")).otherwise(lit(\"\")).as(\"rowTag\")"
                  + ",col(\"properties\").as(\"properties\")"
                  + ",col(\"resource\"))\n");
        sb.append("fileMetadataDf");

        return sb.toString();

    }

    public static void main(String[] args) {

        String str = FileMetadataScalaScriptGenerator.getScript(new String[]{"file:///var/kylo/application.properties", "file:///var/kylo/"});
        int i = 0;

    }

}
