package com.thinkbiganalytics.kylo.utils;

/*-
 * #%L
 * kylo-spark-livy-core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.kylo.spark.SparkException;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobRequest;
import com.thinkbiganalytics.spark.rest.model.PageSpec;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

public class ScalaScriptService {

    private static Pattern dfPattern = Pattern.compile("^\\s*df\\s*$", Pattern.MULTILINE);

    @Resource
    private ScriptGenerator scriptGenerator;

    @Nonnull
    public String wrapScriptForLivy(@Nonnull final SparkJobRequest request) {
        // Add parent reference
        final StringBuilder livyScript = new StringBuilder();
        if (request.getParent() != null && request.getParent().getId() != null) {
            livyScript.append("var parent = sqlContext.read.table(\"").append(StringEscapeUtils.escapeJava(request.getParent().getId())).append("\")\n");
        }

        // Extract last line
        if (StringUtils.isBlank(request.getScript())) {
            throw new SparkException("Spark job script cannot be blank");
        }

        String lastLine = "";
        String script = request.getScript();
        while (StringUtils.isBlank(lastLine)) {
            final int index = script.lastIndexOf('\n');
            lastLine = script.substring(index + 1);
            script = script.substring(0, index);
        }

        // Generate script for Livy
        livyScript.append(script).append('\n')
            .append("var result = mapper.writeValueAsString(").append(lastLine).append(")\n")
            .append("%json result");
        return livyScript.toString();
    }

    /**
     * Modifies the script in request (assumed that it contains a dataframe named df), and creates a
     * List(schema,dataRows) object.  schema is a scala string of json representing the schema.  dataRows
     * is a List of Lists of the row data.  The columns and rows are paged according to the data provided
     * in request.pageSpec.
     */
    public String wrapScriptForLivy(TransformRequest request, String transformId) {

        String newScript;
        if (request.isDoProfile()) {
            newScript = scriptGenerator.script("profileDataFrame", setParentVar(request));
        } else {
            newScript = dataFrameWithSchema(request, transformId);
        }

        return newScript;
    }

    private String dataFrameWithSchema(TransformRequest request, String transformId) {
        String script = request.getScript();

        StringBuilder sb = new StringBuilder();
        if (request.getParent() != null) {
            sb.append(setParentVar(request));
        } // end if

        // String transformId = ScalaScriptService.newTableName();
        // transformCache.put(request, transformId);

        script = dfPattern.matcher(script).replaceAll(/*"var df" + counter + " = df.cache(); "df" + counter*/
            "df = df.cache(); df.registerTempTable( \"" + transformId + "\" )\n");

        sb.append(wrapScriptWithPaging(script, request.getPageSpec()));

        sb.append("val dfRowsAsJson = mapper.writeValueAsString(dfRows)\n");
        sb.append("%json dfRowsAsJson\n");

        return sb.toString();
    }


    /**
     * Modifies the script passed in (assumed it contains a dataframe named df), and creates a List(schema,dataRows)
     * object.  schema is a scala string of json representing the schema.  dataRows is a List of Lists of the row data.
     * The columns and rows are paged according to the data provided in request.pageSpec
     */
    private String wrapScriptWithPaging(String script, PageSpec pageSpec) {
        if (pageSpec != null) {
            Integer startCol = pageSpec.getFirstCol();
            Integer stopCol = startCol + pageSpec.getNumCols();
            Integer startRow = pageSpec.getFirstRow();
            Integer stopRow = startRow + pageSpec.getNumRows();

            return scriptGenerator.wrappedScript("pagedDataFrame", script, "\n",
                                                 startCol, stopCol, startRow, stopRow);
        } else {
            // No Pagespec so simply return all results
            return script.concat("val dfRows = List( df.schema.json, df.rdd.collect.map(x => x.toSeq) )\n");
        }
    }


    /**
     * Generates a new, unique table name.
     *
     * @return the table name
     * @throws IllegalStateException if a table name cannot be generated
     */
    public static String newTableName() {
        for (int i = 0; i < 100; ++i) {
            final String name = UUID.randomUUID().toString();
            if (name.matches("^[a-fA-F].*")) {
                return name.replace("-", "");
            }
        }
        throw new IllegalStateException("Unable to generate a new table name");
    }


    private static String setParentVar(TransformRequest request) {
        String table = request.getParent().getTable();
        if (StringUtils.isEmpty(table)) {
            return request.getParent().getScript();
        } else {
            return String.format("var parent = sqlContext.sql(\"SELECT * FROM %s\")\n", request.getParent().getTable());
        }
    }
}
