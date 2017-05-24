package com.thinkbiganalytics.metadata.rest.model.data;

/*-
 * #%L
 * kylo-metadata-rest-model
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
import com.fasterxml.jackson.annotation.JsonSubTypes;

/**
 * Defines a data source managed through Kylo by a user.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSubTypes(@JsonSubTypes.Type(JdbcDatasource.class))
public class UserDatasource extends Datasource implements com.thinkbiganalytics.metadata.datasource.UserDatasource {

    /**
     * Type name of this data source
     */
    private String type;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
}
