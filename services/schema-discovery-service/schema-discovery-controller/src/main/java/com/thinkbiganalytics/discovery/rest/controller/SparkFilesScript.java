package com.thinkbiganalytics.discovery.rest.controller;

/*-
 * #%L
 * thinkbig-schema-discovery-controller
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

public class SparkFilesScript {

    String parserDescriptor;
    List<String> files;
    String dataFrameVariable;

    public SparkFilesScript(){

    }

    public String getParserDescriptor() {
        return parserDescriptor;
    }

    public void setParserDescriptor(String parserDescriptor) {
        this.parserDescriptor = parserDescriptor;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public String getDataFrameVariable() {
        return dataFrameVariable;
    }

    public void setDataFrameVariable(String dataFrameVariable) {
        this.dataFrameVariable = dataFrameVariable;
    }
}
