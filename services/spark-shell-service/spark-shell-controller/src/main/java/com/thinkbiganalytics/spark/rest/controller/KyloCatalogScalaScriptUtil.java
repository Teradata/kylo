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

import com.thinkbiganalytics.spark.rest.model.PreviewDataSetRequest;
import com.thinkbiganalytics.spark.rest.model.KyloCatalogReadRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

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

    public static String startingScript(){

        StringBuilder sb = new StringBuilder();
        sb.append("import org.apache.spark.sql._\n");
        sb.append(" import com.thinkbiganalytics.kylo.catalog._\n");
        sb.append(" var builder = KyloCatalog.builder(sqlContext)\n");
        sb.append("var kyloClient = builder.build()\n");
        return sb.toString();
    }



    public static String asScalaScript(PreviewDataSetRequest request, String dataFrameVar, boolean prependStartingScript, boolean appendTrailingDataFrameVar){
       KyloCatalogReadRequest kyloCatalogReadRequest = KyloCatalogReaderUtil.toKyloCatalogRequest(request);
       return asScalaScript(kyloCatalogReadRequest,dataFrameVar,prependStartingScript,appendTrailingDataFrameVar);
    }

private static StringBuilder appendNotNull(StringBuilder sb, String str){
        if(StringUtils.isNotBlank(str)){
            sb.append(str);
        }
        return sb;
}
    public static String asScalaScript(KyloCatalogReadRequest request,String dataFrameVar, boolean prependStartingScript, boolean appendTrailingDataFrameVar){

        Integer previewLimit = request.getPageSpec() != null ? request.getPageSpec().getNumRows() : null;
        String format = request.getFormat();
        String addFiles = request.getFiles().isEmpty()?null :asOptions(request.getFiles(),"addFile");
        String addJars = request.getJars().isEmpty()? null : asOptions(request.getJars(),"addJar");
        String addOptions = request.getOptions().isEmpty()? null : asOptions(request.getOptions(),"option");
        String limit = previewLimit != null ? String.format(".limit(%s)",previewLimit):"";

        StringBuilder script = new StringBuilder();

        if(prependStartingScript){
            String start = startingScript();
            script.append(start);
        }

        if(StringUtils.isNotBlank(dataFrameVar)) {
            script.append("var " + dataFrameVar + " = ");
        }
          script.append("kyloClient.read()");
          script.append(String.format(".format(\"%s\")",format));
          appendNotNull(script,addOptions);
          appendNotNull(script,addFiles);
          appendNotNull(script,addJars);
            if(request.hasPaths()){
                script.append(String.format(".load(%s)\n",StringUtils.join(request.getPaths(),"\",\"")));
            }
            else {
                script.append(".load()");
            }
            script.append(limit);


        if(appendTrailingDataFrameVar){
            script.append(dataFrameVar);
        }


       return script.toString();

    }

}
