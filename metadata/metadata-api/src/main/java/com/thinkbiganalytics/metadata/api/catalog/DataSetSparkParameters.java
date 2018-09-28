/**
 * 
 */
package com.thinkbiganalytics.metadata.api.catalog;

/*-
 * #%L
 * kylo-metadata-api
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

import java.util.List;
import java.util.Map;

/**
 * The parameters passed to spark when a data frame representing the data set is created. 
 */
public interface DataSetSparkParameters {
    
    /**
     * @return the format
     */
    String getFormat();
    
    /**
     * @param format the format
     */
    void setFormat(String format);

    /**
     * @return files to be placed in the working directory of each executor
     */
    List<String> getFiles();

    /**
     * @return Jar files to include on the driver and executor classpaths
     */
    List<String> getJars();

    /**
     * @return File paths (and URIs) for input data
     */
    List<String> getPaths();
    
    /**
     * @return Input or output options for the data source
     */
    Map<String, String> getOptions();

    /**
     * Adds/replaces an option value.
     * @param name option name
     * @param value option value
     * @return true if the option was added or updated
     */
    boolean addOption(String name, String value);
    
    /**
     * Removes an option
     * @param name option name
     * @return the value that was assigned to the option, or null if not present
     */
    String removeOption(String name);
    
    /**
     * Removes all options.
     * @return true if there were at least one option removed
     */
    boolean clearOptions();
}
