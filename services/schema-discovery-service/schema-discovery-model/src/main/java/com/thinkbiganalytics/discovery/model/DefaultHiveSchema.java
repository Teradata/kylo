package com.thinkbiganalytics.discovery.model;

/*-
 * #%L
 * thinkbig-schema-discovery-model2
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.discovery.schema.HiveTableSchema;

/**
 * The model used to pass the hive schema
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultHiveSchema extends DefaultTableSchema implements HiveTableSchema {

    private String hiveFormat;

    private Boolean structured = false;

    @Override
    public String getHiveFormat() {
        return hiveFormat;
    }

    @Override
    public void setHiveFormat(String hiveFormat) {
        this.hiveFormat = hiveFormat;
    }

    @Override
    public Boolean isStructured() {
        return structured;
    }

    @Override
    public void setStructured(boolean structured) {
        this.structured = true;
    }
}


