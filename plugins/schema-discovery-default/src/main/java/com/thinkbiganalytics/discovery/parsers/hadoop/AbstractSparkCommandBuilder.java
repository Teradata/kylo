package com.thinkbiganalytics.discovery.parsers.hadoop;

import java.util.List;

import static com.thinkbiganalytics.discovery.parser.SparkFileSchemaParser.NO_LIMIT;

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
public abstract class AbstractSparkCommandBuilder implements SparkCommandBuilder {

    protected String dataframeVariable;
    private Integer limit = 10;

    public AbstractSparkCommandBuilder(String dataframeVariable, Integer limit) {
        this.dataframeVariable = dataframeVariable;
        this.limit = limit;
    }


    public AbstractSparkCommandBuilder() {
    }

    public String getDataframeVariable() {
        return dataframeVariable;
    }

    public void setDataframeVariable(String dataframeVariable) {
        this.dataframeVariable = dataframeVariable;
    }

    public boolean isUseDataFrameVariable() {
        return dataframeVariable != null;
    }


    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public boolean isLimit() {
        return limit != null && NO_LIMIT != limit;
    }


    public void appendDataFrameScript(StringBuilder sb, String dataframeVariable, String method, String pathToFile, boolean appendLimit) {
        appendDataFrameReadScript(dataframeVariable,sb,method,pathToFile);
        if(appendLimit){
            appendLimit(sb);
        }
        sb.append(".toDF()");
    }

    public void appendLimit(StringBuilder sb){
        if(isLimit()) {
            sb.append(String.format(".limit(%s)", limit));
        }
    }

    @Override
    public String appendLimit(String script) {
        StringBuilder sb = new StringBuilder(script);
        if(isLimit()){
            sb.append(dataframeVariable + " = "+dataframeVariable );
            sb.append(String.format(".limit(%s)", limit));
        }
        return sb.toString();

    }

    public SparkCommandBuilder dataFrameVariableEquals(String dataframeVariable,StringBuilder sb){
        sb.append((dataframeVariable != null ? "var " + dataframeVariable + " = " : ""));
        return this;
    }

    public SparkCommandBuilder appendDataFrameVariable(String dataframeVariable,StringBuilder sb){
        sb.append((dataframeVariable != null ? "var " + dataframeVariable + " = " : ""));
        return this;
    }

    public SparkCommandBuilder appendDataFrameReadScript(StringBuilder sb, String method, String pathToFile){
        sb.append(String.format("sqlContext.read.%s(\"%s\")", method, pathToFile));
        return this;
    }

    public void appendDataFrameReadScript(String dataframeVariable,StringBuilder sb, String method, String pathToFile){
        appendDataFrameVariable(dataframeVariable,sb);
        sb.append(String.format("sqlContext.read.%s(\"%s\")", method, pathToFile));
    }

    /**
     * unionDataFrames("sqlContext.read.avro(\"%s\").toDF()\n")
     * @param scriptToParse
     * @param paths
     * @return
     */
    public String unionDataFrames(List<String> paths,String scriptToParse,  Object... args){
        StringBuilder sb = new StringBuilder();
        int counter = 0;
        String tmpDf = "tmpDf";
        for(String path: paths) {
            String df = counter ==0 ? "var tmpDf = " : counter == 1 ? "var df1 = " : "df1 = ";
            String script = df + scriptToParse;
            Object[] scriptArgs = null;
            if(args != null) {
                scriptArgs = add(args, path);
            }
            else {
                scriptArgs = new Object[] {path};
            }
            sb.append(String.format(script, (Object[])scriptArgs));
            if(counter >0){
                sb.append(String.format("%s = %s.unionAll(%s)\n", tmpDf,tmpDf,"df1"));
            }
            counter++;
        }
        if(dataframeVariable == null) {
            dataframeVariable = "df";
        }
        sb.append(String.format("var %s = %s\n",dataframeVariable,tmpDf));
        if(isLimit()) {
            sb.append(String.format("%s = %s.limit(%s)\n", dataframeVariable, dataframeVariable, getLimit()));
        }
        return sb.toString();

    }
    private static Object[] add(Object[] arr, Object... elements){
        Object[] tempArr = new Object[arr.length+elements.length];
        System.arraycopy(arr, 0, tempArr, 0, arr.length);

        for(int i=0; i < elements.length; i++)
            tempArr[arr.length+i] = elements[i];
        return tempArr;

    }

}
