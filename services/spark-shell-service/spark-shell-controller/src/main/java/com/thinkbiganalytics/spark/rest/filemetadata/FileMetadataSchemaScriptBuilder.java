package com.thinkbiganalytics.spark.rest.filemetadata;

/*-
 * #%L
 * kylo-spark-shell-controller
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

import java.util.List;

/**
 * Generates the Scala script needed to get the file type schema
 */
public class FileMetadataSchemaScriptBuilder {


    public static String getSparkScript(String type, String rowTag, List<String> files) {

        StringBuilder startScript = new StringBuilder();

        startScript.append("import com.thinkbiganalytics.kylo.catalog._\n");
        startScript.append("var kyloClientBuilder = KyloCatalog.builder(sqlContext) \n");
        startScript.append("var kyloClient = kyloClientBuilder.build()\n");


        StringBuilder sb = new StringBuilder();
        if (type == "application/xml") {
            sb.append("kyloClient.read.option(\"mimeType\",\"%s\").option(\"rowTag\",\"%s\").format(\"com.thinkbiganalytics.spark.file.metadata.schema\").load(\"%s\")\n");
            String script = sb.toString();
            startScript.append(unionDataFrames(files, script, type, rowTag));
            return startScript.toString();
        } else {
            sb.append("kyloClient.read.option(\"mimeType\",\"%s\").format(\"com.thinkbiganalytics.spark.file.metadata.schema\").load(\"%s\")\n");
            String script = sb.toString();
            startScript.append(unionDataFrames(files, script, type));
            return startScript.toString();
        }
    }

    public static String unionDataFrames(List<String> paths, String scriptToParse, Object... args) {
        String finalDf = "df";
        StringBuilder sb = new StringBuilder();
        int counter = 0;
        String tmpDf = "tmpDf";
        for (String path : paths) {
            String df = counter == 0 ? "var tmpDf = " : counter == 1 ? "var df1 = " : "df1 = ";
            String script = df + scriptToParse;
            Object[] scriptArgs = null;
            if (args != null) {
                scriptArgs = add(args, path);
            } else {
                scriptArgs = new Object[]{path};
            }
            sb.append(String.format(script, (Object[]) scriptArgs));
            if (counter > 0) {
                sb.append(String.format("%s = %s.unionAll(%s);\n", tmpDf, tmpDf, "df1"));
            }
            counter++;
        }

        sb.append(String.format("var %s = %s;\n%s", finalDf, tmpDf, finalDf));
        return sb.toString();

    }

    private static Object[] add(Object[] arr, Object... elements) {
        Object[] tempArr = new Object[arr.length + elements.length];
        System.arraycopy(arr, 0, tempArr, 0, arr.length);

        for (int i = 0; i < elements.length; i++) {
            tempArr[arr.length + i] = elements[i];
        }
        return tempArr;

    }

}
