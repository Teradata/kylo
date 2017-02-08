/**
 *
 */
package com.thinkbiganalytics.metadata.rest.model.data;

/*-
 * #%L
 * thinkbig-metadata-rest-model
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

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class DirectoryDatasource extends Datasource {

    private String path;
    private Set<FilePattern> patterns = new HashSet<>();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<FilePattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(Set<FilePattern> patterns) {
        this.patterns = patterns;
    }

    public void addGlobPattern(String pattern) {
        FilePattern fp = new FilePattern();
        fp.setGlob(pattern);
        this.patterns.add(fp);
    }

    public void addRegexPattern(String pattern) {
        FilePattern fp = new FilePattern();
        fp.setRegex(pattern);
        this.patterns.add(fp);
    }
}
