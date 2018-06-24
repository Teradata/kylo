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
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.spark.rest.model.Datasource;
import com.thinkbiganalytics.spark.rest.model.JdbcDatasource;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.SQLContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * A standard data source provider compatible with multiple versions of Spark.
 *
 * @param <T> the Spark data set type
 */
public abstract class AbstractCatalogDataSetProvider<T> implements CatalogDataSetProvider<T> {

    /**
     * Map of id to data source.
     */
    @Nonnull
    private final Map<String, DataSet> dataSets;

    /**
     * Constructs an {@code AbstractDatasourceProvider} with the specified data sources.
     *
     * @param datasources the data sources
     */
    public AbstractCatalogDataSetProvider(@Nonnull final Collection<DataSet> dataSets) {
        this.dataSets = new HashMap<>(dataSets.size());
        for (final DataSet dataSet : dataSets) {
            this.dataSets.put(dataSet.getId(), dataSet);
        }
    }

    @Nonnull
    @Override
    public DataSet findById(@Nonnull final String id) {
        final DataSet dataSet = dataSets.get(id);
        if (dataSet != null) {
            return dataSet;
        } else {
            throw new IllegalArgumentException("DataSet does not exist: " + id);
        }
    }


    public abstract T readDataSet(DataSet dataSet);

    public T read(String datasetId){
        DataSet dataSet = findById(datasetId);
        return readDataSet(dataSet);
    }


    public Map<String,String> mergeOptions(Map<String,String> templateOptions, Map<String,String>dataSetOptions){
        Map<String,String> allOptions = new HashMap<String,String>();
        if(templateOptions != null){
            allOptions.putAll(templateOptions);
        }
        if(dataSetOptions != null){
            allOptions.putAll(dataSetOptions);
        }
        return allOptions.isEmpty() ? null : allOptions;
    }
}
