/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.rest.model;

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

import com.thinkbiganalytics.metadata.api.catalog.DataSet;
import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Map.Entry;
import java.util.function.Function;

/**
 *
 */
// TODO: This should be moved back into the kylo-catalog-controller module after the dependencies are worked out
@Component
public class CatalogModelTransform {

    public CatalogModelTransform() {
        super();
    }

    public Function<com.thinkbiganalytics.metadata.api.catalog.Connector, Connector> connectorToRestModel() {
        return (domain) -> {
            com.thinkbiganalytics.kylo.catalog.rest.model.Connector model = new com.thinkbiganalytics.kylo.catalog.rest.model.Connector();
            model.setId(domain.getId().toString());
            model.setTitle(domain.getTitle());
            model.setDescription(domain.getDescription());
            model.setPluginId(domain.getPluginId());
            model.setIcon(domain.getIcon());
            model.setColor(domain.getIconColor());
            model.setTemplate(sparkParamsToRestModel().apply(domain.getSparkParameters()));
            return model;
        };
    }

    /**
     * @return
     */
    private Function<DataSetSparkParameters, DataSetTemplate> sparkParamsToRestModel() {
        return (domain) -> {
            DefaultDataSetTemplate model = new DefaultDataSetTemplate();
            model.setFormat(domain.getFormat());
            model.setPaths(domain.getPaths());
            model.setFiles(domain.getFiles());
            model.setJars(domain.getJars());
            model.setOptions(domain.getOptions());
            return model;
        };
    }
    
    public void updateDataSet(com.thinkbiganalytics.kylo.catalog.rest.model.DataSet model, DataSet domain) {
        domain.setTitle(model.getTitle());
        domain.setDescription(generateDescription(model));
        updateSparkParameters(model, domain.getSparkParameters());
    }
    
    /**
     * @param model
     * @param sparkParameters
     */
    public void updateSparkParameters(DataSetTemplate model, DataSetSparkParameters sparkParams) {
        sparkParams.setFormat(model.getFormat());
        sparkParams.getFiles().retainAll(model.getFiles());
        sparkParams.getJars().retainAll(model.getJars());
        sparkParams.getPaths().retainAll(model.getPaths());
        
        sparkParams.clearOptions();
        for (Entry<String, String> entry : model.getOptions().entrySet()) {
            sparkParams.addOption(entry.getKey(), entry.getValue());
        }
    }
    
    public void updateDataSource(DataSource model, com.thinkbiganalytics.metadata.api.catalog.DataSource domain) {
        domain.setTitle(model.getTitle());
        domain.setDescription(generateDescription(model));
        updateSparkParameters(model.getTemplate(), domain.getSparkParameters());
    }

    /**
     * @return
     */
    public Function<DataSet, com.thinkbiganalytics.kylo.catalog.rest.model.DataSet> dataSetToRestModel() {
        return (domain) -> {
            com.thinkbiganalytics.kylo.catalog.rest.model.DataSet dataSet = new com.thinkbiganalytics.kylo.catalog.rest.model.DataSet();
            dataSet.setId(domain.getId().toString());
            dataSet.setDataSource(dataSourceToRestModel().apply(domain.getDataSource()));
            dataSet.setTitle(domain.getTitle());
            // TODO: add description
            DataSetTemplate template = sparkParamsToRestModel().apply(domain.getSparkParameters());
            dataSet.setFormat(template.getFormat());
            dataSet.setOptions(template.getOptions());
            dataSet.setPaths(template.getPaths());
            return dataSet;
        };
    }

    /**
     * @return
     */
    public Function<com.thinkbiganalytics.metadata.api.catalog.DataSource, DataSource> dataSourceToRestModel() {
        return (domain) -> {
            DataSource model = new DataSource();
            model.setId(domain.getId().toString());
            model.setTitle(domain.getTitle());
            model.setConnector(connectorToRestModel().apply(domain.getConnector()));
            model.setTemplate(sparkParamsToRestModel().apply(domain.getSparkParameters()));
            return model;
        };
    }
    
    /**
     * @return a domain to REST model converter for use by a Page
     */
    public Converter<com.thinkbiganalytics.metadata.api.catalog.DataSource, DataSource> convertDataSourceToRestModel() {
        return (domain) -> dataSourceToRestModel().apply(domain);
    }
    
    private String generateDescription(DataSource dataSource) {
        return "";
    }

    private String generateDescription(com.thinkbiganalytics.kylo.catalog.rest.model.DataSet dataSet) {
        return "";
    }
}
