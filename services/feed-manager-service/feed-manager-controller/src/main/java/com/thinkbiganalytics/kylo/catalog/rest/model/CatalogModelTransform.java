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
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Map.Entry;
import java.util.function.Function;

import javax.inject.Inject;

/**
 *
 */
// TODO: This should be moved back into the kylo-catalog-controller module after the dependencies are worked out
@Component
public class CatalogModelTransform {
    
    @Inject
    private SecurityModelTransform securityTransform;

    public CatalogModelTransform() {
        super();
    }
    
    public void setSecurityTransform(SecurityModelTransform securityTransform) {
        this.securityTransform = securityTransform;
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
//            securityTransform.applyAccessControl(domain, model);
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
    
    public DataSet updateDataSet(com.thinkbiganalytics.kylo.catalog.rest.model.DataSet model, DataSet domain) {
        domain.setTitle(model.getTitle());
        domain.setDescription(generateDescription(model));
        updateSparkParameters(model, domain.getSparkParameters());
        return domain;
    }
    
    /**
     * @param model
     * @param sparkParameters
     */
    public void updateSparkParameters(DataSetTemplate model, DataSetSparkParameters sparkParams) {
        sparkParams.setFormat(model.getFormat());
        if (model.getFiles() != null) sparkParams.getFiles().retainAll(model.getFiles());
        if (model.getJars() != null) sparkParams.getJars().retainAll(model.getJars());
        if (model.getPaths() != null) sparkParams.getPaths().retainAll(model.getPaths());
        
        if (model.getOptions() != null) {
            sparkParams.clearOptions();
            for (Entry<String, String> entry : model.getOptions().entrySet()) {
                sparkParams.addOption(entry.getKey(), entry.getValue());
            }
        }
    }
    
    public com.thinkbiganalytics.metadata.api.catalog.DataSource updateDataSource(DataSource model, com.thinkbiganalytics.metadata.api.catalog.DataSource domain) {
        domain.setTitle(model.getTitle());
        domain.setDescription(generateDescription(model));
        updateSparkParameters(model.getTemplate(), domain.getSparkParameters());
        return domain;
    }

    /**
     * @return
     */
    public Function<DataSet, com.thinkbiganalytics.kylo.catalog.rest.model.DataSet> dataSetToRestModel() {
        return (domain) -> {
            com.thinkbiganalytics.kylo.catalog.rest.model.DataSet model = new com.thinkbiganalytics.kylo.catalog.rest.model.DataSet();
            model.setId(domain.getId().toString());
            model.setDataSource(dataSourceToRestModel().apply(domain.getDataSource()));
            model.setTitle(domain.getTitle());
            // TODO: add description
            DataSetTemplate template = sparkParamsToRestModel().apply(domain.getSparkParameters());
            model.setFormat(template.getFormat());
            model.setOptions(template.getOptions());
            model.setPaths(template.getPaths());
//            securityTransform.applyAccessControl(domain, model);
            return model;
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
            securityTransform.applyAccessControl(domain, model);
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
