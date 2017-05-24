package com.thinkbiganalytics.metadata.datasource;

/*-
 * #%L
 * Kylo Metadata Service API
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
 * A storage for data. Can be either a source or a destination.
 */
public interface Datasource {

    /**
     * Gets the unique id for this data source.
     *
     * @return the data source id
     */
    String getId();

    /**
     * Sets the unique id for this data source.
     *
     * @param id the data source id
     */
    void setId(String id);

    /**
     * Gets the human-readable name for this data source.
     *
     * @return the name
     */
    String getName();

    /**
     * Sets the human-readable name for this data source.
     *
     * @param name the name
     */
    void setName(String name);

    /**
     * Gets the description of this data source.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Sets the description of this data source.
     *
     * @param description the description
     */
    void setDescription(String description);
}
