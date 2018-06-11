package com.thinkbiganalytics.spark.rest.controller;

/*-
 * #%L
 * Spark Shell Service Controllers
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

import com.thinkbiganalytics.spark.rest.model.KyloCatalogReadRequest;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility to convert a Kylo Catalog Read Request into Scala Script to return a dataframe
 */
public class KyloCatalogScalaScriptUtil {



    private static String asOptions(List<String> options, String var){
        return options != null ? options.stream().collect(Collectors.joining("\")." + var + "(\"", "." + var + "(\"", "\")")) : "";
    }
    private static String asOptions(Map<String,String> options, String var) {
        return options != null && !options.isEmpty() ? options.entrySet().stream().map(entrySet -> "\"" + entrySet.getKey() + "\",\"" + StringEscapeUtils.escapeJava(entrySet.getValue()) + "\"").collect(Collectors.joining(")." + var + "(", "." + var + "(", ")")) : "";
    }


    public static String asScalaScript(KyloCatalogReadRequest request){

        int previewLimit = request.getPageSpec() != null ? request.getPageSpec().getNumRows() : 20;
        String format = request.getFormat();

        StringBuilder sb = new StringBuilder();
        sb.append("import org.apache.spark.sql._\n");
        sb.append(" import com.thinkbiganalytics.kylo.catalog._\n");
        sb.append(" var builder = KyloCatalog.builder(sqlContext)\n");
        sb.append("var client = builder.build()\n");

        String addFiles = asOptions(request.getFiles(),"addFile");
        String addJars = asOptions(request.getJars(),"addJar");
        String addOptions = asOptions(request.getOptions(),"option");
        String path = request.getPaths() != null && !request.getPaths().isEmpty() ? request.getPaths().get(0) : "";

        sb.append(String.format(" var reader = client.read%s.format(\"%s\")%s%s\n",addOptions,format,addFiles,addJars));
        sb.append("var df = reader.load().limit("+previewLimit+")\n");
        sb.append("df");


        String script = sb.toString();
        return script;

    }

}
