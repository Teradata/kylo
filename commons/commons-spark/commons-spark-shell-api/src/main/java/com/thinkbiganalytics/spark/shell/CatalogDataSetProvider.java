package com.thinkbiganalytics.spark.shell;

/*-
 * #%L
 * Kylo Commons Spark Shell
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

import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.spark.rest.model.Datasource;

import org.apache.spark.sql.SQLContext;

import javax.annotation.Nonnull;

/**
 * Provides instances of {@link Datasource}.
 */
public interface CatalogDataSetProvider<T> {

    /**
     * Gets the data source with the specified id.
     *
     * @param id the data source id
     * @return the data source, if found
     */
    @Nonnull
    DataSet findById(@Nonnull String id);

    T readDataSet(DataSet dataSet);

    T read(String dataSetId);

    /**
     * remap this dataSetId and return the supplied dataframe instead of executing the read function
     * @param dataSetId
     * @param dataFrame
     * @return
     */
    void remap(String dataSetId, T dataFrame);
}
