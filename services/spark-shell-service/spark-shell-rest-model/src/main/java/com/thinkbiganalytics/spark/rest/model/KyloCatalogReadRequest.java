package com.thinkbiganalytics.spark.rest.model;

/*-
 * #%L
 * Spark Shell Service REST Model
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KyloCatalogReadRequest {

    List<String> jars;

    List<String> files;

    List<String> paths;

    String format;

    Map<String,String> options;

    private Integer limit;

    /**
     * Constrain the results returned to the client
     */
    private PageSpec pageSpec;

    public List<String> getJars() {
        if(jars == null){
            jars = new ArrayList<>();
        }
        return jars;
    }

    public void setJars(List<String> jars) {
        this.jars = jars;
    }

    public List<String> getFiles() {
        if(files == null){
            files = new ArrayList<>();
        }
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Map<String, String> getOptions() {
        if(options == null){
            options = new HashMap<>();
        }
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public List<String> getPaths() {
        if(paths == null){
            paths = new ArrayList<>();
        }
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public void addJar(String jar){
        getJars().add(jar);
    }

    public void addFile(String file){
        getFiles().add(file);
    }

    public void addPath(String path){
        getPaths().add(path);
    }

    public void addOption(String key, String value){
        getOptions().put(key,value);
    }

    public PageSpec getPageSpec() {
        return pageSpec;
    }

    public void setPageSpec(PageSpec pageSpec) {
        this.pageSpec = pageSpec;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public boolean hasPaths(){
        return !getPaths().isEmpty();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("KyloCatalogReadRequest{");
        sb.append("jars=").append(jars);
        sb.append(", files=").append(files);
        sb.append(", paths=").append(paths);
        sb.append(", format='").append(format).append('\'');
        sb.append(", options=").append(options);
        sb.append(", limit=").append(limit);
        sb.append(", pageSpec=").append(pageSpec);
        sb.append('}');
        return sb.toString();
    }
}
