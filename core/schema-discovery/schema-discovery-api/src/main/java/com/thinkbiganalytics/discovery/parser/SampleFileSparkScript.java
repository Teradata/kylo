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

/**
 * Dto class to hold the uploaded file and the generated script.
 */
public class SampleFileSparkScript {

    /**
     * the file location of the sample file on the edge node
     */
    private String fileLocation;
    /**
     * the generated spark script
     */
    private String script;

    public SampleFileSparkScript() {

    }

    public SampleFileSparkScript(String fileLocation, String script) {
        this.fileLocation = fileLocation;
        this.script = script;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
