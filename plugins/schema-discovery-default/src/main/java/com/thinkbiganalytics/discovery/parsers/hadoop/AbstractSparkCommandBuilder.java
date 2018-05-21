package com.thinkbiganalytics.discovery.parsers.hadoop;

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

    public void appendDataFrameScript(StringBuilder sb, String method, String pathToFile) {
        appendDataFrameReadScript(sb,method,pathToFile);
        if (isLimit()) {
            sb.append(String.format(".limit(%s)", limit));
        }
        sb.append(".toDF()");
    }

    public void appendDataFrameVariable(StringBuilder sb){
        sb.append((dataframeVariable != null ? "var " + dataframeVariable + " = " : ""));
    }

    public void appendDataFrameReadScript(StringBuilder sb, String method, String pathToFile){
        appendDataFrameVariable(sb);
        sb.append(String.format("sqlContext.read.%s(\"%s\")", method, pathToFile));
    }
}
