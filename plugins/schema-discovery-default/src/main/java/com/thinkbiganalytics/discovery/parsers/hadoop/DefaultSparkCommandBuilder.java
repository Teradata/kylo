package com.thinkbiganalytics.discovery.parsers.hadoop;

/*-
 * #%L
 * DefaultSparkCommandBuilder.java
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

public class DefaultSparkCommandBuilder extends AbstractSparkCommandBuilder {

    private String method;

    public DefaultSparkCommandBuilder(String method) {
        this.method = method;
    }

    public DefaultSparkCommandBuilder(String dataframeVariable, Integer limit, String method) {
        super(dataframeVariable, limit);
        this.method = method;
    }

    @Override
    public String build(String pathToFile) {
        StringBuilder sb = new StringBuilder();
        sb.append((dataframeVariable != null ? "var " + dataframeVariable + " = " : ""));
        sb.append(String.format("sqlContext.read.%s(\"%s\")", method, pathToFile));
        if(isLimit()) {
            sb.append(String.format(".limit(%s)", getLimit()));
        }
        sb.append(".toDF()");
        return sb.toString();
    }

    public String build(List<String> paths) {

        return unionDataFrames(paths,"sqlContext.read.%s(\"%s\").toDF()\n",method);

    }

}
