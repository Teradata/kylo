package com.thinkbiganalytics.discovery.parsers.hadoop;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.spring.SpringApplicationContext;

import javax.inject.Inject;

public class AbstractSparkFileSchemaParser {

    @Inject
    @JsonIgnore
    private transient SparkFileSchemaParserService parserService;

    public SparkFileSchemaParserService getSparkParserService() {
        // Since this class is created by reflection we need to call autowire
        if (parserService == null) {
            SpringApplicationContext.autowire(this);
        }
        return parserService;
    }

}
