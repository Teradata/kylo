package com.thinkbiganalytics.kylo.catalog.rest.model;

/*-
 * #%L
 * kylo-catalog-model
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
 * Template for creating a new data set
 */
public interface DataSetTemplate {

    /**
     * Files to be placed in the working directory of each executor
     */
    List<String> getFiles();

    /**
     * Short name or class name of the Spark data source used by this connector
     */
    String getFormat();

    /**
     * Jar files to include on the driver and executor classpaths
     */
    List<String> getJars();

    /**
     * Input or output options for the data source
     */
    Map<String, String> getOptions();

    /**
     * File paths (and URIs) for input data
     */
    List<String> getPaths();
}
