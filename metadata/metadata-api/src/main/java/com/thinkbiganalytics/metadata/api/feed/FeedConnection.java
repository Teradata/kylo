/**
 *
 */
package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.api.catalog.DataSet;

/*-
 * #%L
 * thinkbig-metadata-api
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

import com.thinkbiganalytics.metadata.api.datasource.Datasource;

import java.util.Optional;

/**
 * A feed may be connected to a the legacy entity Datasource or the new entity DataSet, so either
 * one is optional but one may must contain a value.
 * TODO: Remove the Optional when Datasource is removed from the model.
 */
public interface FeedConnection {

    Feed getFeed();

    /**
     * @return an optional associated Datasource 
     */
    Optional<Datasource> getDatasource();
    
    /**
     * @return an optional associated DataSet 
     */
    Optional<DataSet> getDataSet();
}
