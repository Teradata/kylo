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


import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogReader;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.catalog.rest.model.DefaultDataSetTemplate;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
     * Map of any datasets that got remapped to a different dataframe
     */
    private final Map<String,T> remappedDataSets;

    /**
     * Constructs an {@code AbstractDatasourceProvider} with the specified data sources.
     *
     * @param dataSets the data sets
     */
    public AbstractCatalogDataSetProvider(@Nonnull final Collection<DataSet> dataSets) {
        this.dataSets = new HashMap<>(dataSets.size());
        this.remappedDataSets = new HashMap<>();
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

    protected abstract KyloCatalogClient<T> getClient();

    public T readDataSet(@Nonnull final DataSet dataSet) {
        final DataSetTemplate dataSetTemplate = mergeTemplates(dataSet);

        KyloCatalogReader<T> reader = getClient().read().options(dataSetTemplate.getOptions()).addJars(dataSetTemplate.getJars()).addFiles(dataSetTemplate.getFiles())
            .format(dataSetTemplate.getFormat());
        T dataFrame;

        if (dataSet.getPaths() != null && !dataSet.getPaths().isEmpty()) {
            if (dataSet.getPaths().size() > 1) {
                dataFrame = reader.load(dataSet.getPaths().toArray(new String[0]));
            } else {
                dataFrame = reader.load(dataSet.getPaths().get(0));
            }
        } else {
            dataFrame = reader.load();
        }
        return dataFrame;
    }

    public T read(String datasetId) {
        if(remappedDataSets.containsKey(datasetId)){
            return remappedDataSets.get(datasetId);
        }
        else {
            DataSet dataSet = findById(datasetId);
            return readDataSet(dataSet);
        }
    }

    /**
     * Merges the data set, data source, and connector templates for the specified data set.
     */
    @Nonnull
    protected DefaultDataSetTemplate mergeTemplates(@Nonnull final DataSet dataSet) {
        final DefaultDataSetTemplate template;

        if (dataSet.getDataSource() != null) {
            final DataSource dataSource = dataSet.getDataSource();
            template = new DefaultDataSetTemplate();

            if (dataSource.getConnector() != null && dataSource.getConnector().getTemplate() != null) {
                mergeTemplates(template, dataSource.getConnector().getTemplate());
            }
            if (dataSource.getTemplate() != null) {
                mergeTemplates(template, dataSource.getTemplate());
            }

            mergeTemplates(template, dataSet);
        } else {
            template = new DefaultDataSetTemplate(dataSet);
        }

        return template;
    }

    /**
     * Merges the specified source template into the specified destination template.
     */
    public void mergeTemplates(@Nonnull final DefaultDataSetTemplate dst, @Nonnull final DataSetTemplate src) {
        if (src.getFiles() != null) {
            if (dst.getFiles() != null) {
                dst.getFiles().addAll(src.getFiles());
            } else {
                dst.setFiles(new ArrayList<>(src.getFiles()));
            }
        }
        if (src.getJars() != null) {
            if (dst.getJars() != null) {
                dst.getJars().addAll(src.getJars());
            } else {
                dst.setJars(new ArrayList<>(src.getJars()));
            }
        }
        if (StringUtils.isNotEmpty(src.getFormat())) {
            dst.setFormat(src.getFormat());
        }
        if (src.getOptions() != null) {
            if (dst.getOptions() != null) {
                dst.getOptions().putAll(src.getOptions());
            } else {
                dst.setOptions(new HashMap<>(src.getOptions()));
            }
        }
        if (src.getPaths() != null) {
            dst.setPaths(src.getPaths());
        }
    }

    @Override
    public void remap(String dataSetId, T dataFrame) {
        this.remappedDataSets.put(dataSetId,dataFrame);
    }
}
