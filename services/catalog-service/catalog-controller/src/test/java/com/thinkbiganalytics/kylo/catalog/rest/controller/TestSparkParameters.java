/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.rest.controller;

/*-
 * #%L
 * kylo-catalog-controller
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

import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test class to be further mocked/spied.
 */
public abstract class TestSparkParameters implements DataSetSparkParameters {

    @Override
    public String getFormat() {
        return "jdbc";
    }

    @Override
    public List<String> getFiles() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getJars() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getPaths() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getOptions() {
        return Collections.emptyMap();
    }

}
