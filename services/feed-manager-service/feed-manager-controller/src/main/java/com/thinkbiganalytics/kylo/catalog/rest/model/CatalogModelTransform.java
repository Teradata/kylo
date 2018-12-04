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

import com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager;
import com.thinkbiganalytics.kylo.catalog.spi.ConnectorPlugin;
import com.thinkbiganalytics.metadata.api.catalog.DataSet;
import com.thinkbiganalytics.metadata.api.catalog.DataSetBuilder;
import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;
import com.thinkbiganalytics.security.core.encrypt.EncryptionService;
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

// TODO: This should be moved back into the kylo-catalog-controller module after the dependencies are worked out
@Component
public class CatalogModelTransform {

    @Inject
    private SecurityModelTransform securityTransform;

    @Inject
    private ConnectorPluginManager pluginManager;

    @Inject
    private EncryptionService encryptionService;

    public CatalogModelTransform() {
        super();
    }

    public CatalogModelTransform(SecurityModelTransform securityTransform, ConnectorPluginManager pluginManager, EncryptionService encryptionService) {
        super();
        this.securityTransform = securityTransform;
        this.pluginManager = pluginManager;
        this.encryptionService = encryptionService;
    }

    public void setSecurityTransform(SecurityModelTransform securityTransform) {
        this.securityTransform = securityTransform;
    }

    public Function<com.thinkbiganalytics.metadata.api.catalog.Connector, Connector> connectorToRestModel() {
        return connectorToRestModel(true, true);
    }

    public Function<com.thinkbiganalytics.metadata.api.catalog.Connector, Connector> connectorToRestModel(final boolean includeTemplate, final boolean encryptedCredentials) {
        return (domain) -> {
            final com.thinkbiganalytics.kylo.catalog.rest.model.Connector model = new com.thinkbiganalytics.kylo.catalog.rest.model.Connector();
            model.setId(domain.getId().toString());
            model.setTitle(domain.getTitle());
            model.setDescription(domain.getDescription());
            model.setPluginId(domain.getPluginId());
            model.setIcon(domain.getIcon());
            model.setColor(domain.getIconColor());
            securityTransform.applyAccessControl(domain, model);

            if (includeTemplate) {
                model.setTemplate(sparkParamsToRestModel(domain.getPluginId(), encryptedCredentials).apply(domain.getSparkParameters()));
            }

            return model;
        };
    }
    
    public com.thinkbiganalytics.kylo.catalog.rest.model.DataSet decryptOptions(com.thinkbiganalytics.kylo.catalog.rest.model.DataSet dataSet) {
        DataSource dataSource = decryptOptions(dataSet.getDataSource());
        dataSet.setDataSource(dataSource);
        return dataSet;
    }
    
    public DataSource decryptOptions(DataSource dataSource) {
        DataSetTemplate template = decryptOptions(dataSource.getTemplate());
        dataSource.setTemplate(template);
        return dataSource;
    }


    
    public DataSetTemplate decryptOptions(DataSetTemplate template) {
        template.getOptions().entrySet().stream()
            .filter(entry -> encryptionService.isEncrypted(entry.getValue()))
            .forEach(entry -> template.getOptions().put(entry.getKey(), encryptionService.decrypt(entry.getValue())));
        return template;
    }

    private Function<DataSetSparkParameters, DataSetTemplate> sparkParamsToRestModel(String connectorPuginId, final boolean encryptedCredentials) {
        return (domain) -> {
            return pluginManager.getPlugin(connectorPuginId)
                .map(plugin -> sparkParamsToRestModel(plugin, encryptedCredentials).apply(domain))
                .orElseThrow(() -> new IllegalArgumentException("No connector plugin with ID: " + connectorPuginId));
        };
    }

    private Function<DataSetSparkParameters, DataSetTemplate> sparkParamsToRestModel(ConnectorPlugin plugin, final boolean encryptedCredentials) {
        return (domain) -> {
            DefaultDataSetTemplate model = new DefaultDataSetTemplate();
            model.setFormat(domain.getFormat());
            model.setPaths(domain.getPaths());
            model.setFiles(domain.getFiles());
            model.setJars(domain.getJars());
            model.setOptions(encryptedCredentials == true ? domain.getOptions() : decryptSensitiveOptions(plugin, domain.getOptions()));
            return model;
        };
    }

    /**
     * Encrypts any sensitive option values based on the appropriate connector plugin settings.
     *
     * @param options the options containing potentially encrypted values
     * @return a new encrypted set of options
     */
    private Map<String, String> encryptSensitiveOptions(ConnectorPlugin plugin, Map<String, String> options) {
        return options.entrySet().stream()
            .map(entry -> {
                if (plugin.isSensitiveOption(entry.getKey()) && !encryptionService.isEncrypted(entry.getValue())) {
                    return new AbstractMap.SimpleEntry<>(entry.getKey(), encryptionService.encrypt(entry.getValue()));
                } else {
                    return entry;
                }
            })
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    /**
     * Decrypts any sensitive option values based on the appropriate connector plugin settings.
     *
     * @param options the options containing potentially encrypted values
     * @return a new decrypted set of options
     */
    private Map<String, String> decryptSensitiveOptions(ConnectorPlugin plugin, Map<String, String> options) {
        return options.entrySet().stream()
            .map(entry -> {
                if (plugin.isSensitiveOption(entry.getKey()) && encryptionService.isEncrypted(entry.getValue())) {
                    return new AbstractMap.SimpleEntry<>(entry.getKey(), encryptionService.decrypt(entry.getValue()));
                } else {
                    return entry;
                }
            })
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public DataSet updateDataSet(com.thinkbiganalytics.kylo.catalog.rest.model.DataSet model, DataSet domain) {
        domain.setTitle(model.getTitle());
        domain.setDescription(generateDescription(model));
        updateSparkParameters(model.getDataSource().getConnector().getPluginId(), model, domain.getSparkParameters());
        return domain;
    }

    public DataSet buildDataSet(com.thinkbiganalytics.kylo.catalog.rest.model.DataSet model, DataSetBuilder builder) {
        return builder
            .title(model.getTitle())
            .format(model.getFormat())
            .addOptions(model.getOptions())
            .addPaths(model.getPaths())
            .addFiles(model.getFiles())
            .addJars(model.getJars())
            .build();
    }

    public DataSetSparkParameters updateSparkParameters(String connectorPuginId, DataSetTemplate model, DataSetSparkParameters sparkParams) {
        return pluginManager.getPlugin(connectorPuginId)
            .map(plugin -> updateSparkParameters(plugin, model, sparkParams))
            .orElseThrow(() -> new IllegalArgumentException("No connector plugin with ID: " + connectorPuginId));
    }

    public DataSetSparkParameters updateSparkParameters(ConnectorPlugin plugin, DataSetTemplate model, DataSetSparkParameters sparkParams) {
        sparkParams.setFormat(model.getFormat());

        if (model.getFiles() != null) {
            sparkParams.getFiles().clear();
            sparkParams.getFiles().addAll(model.getFiles());
        }

        if (model.getJars() != null) {
            sparkParams.getJars().clear();
            sparkParams.getJars().addAll(model.getJars());
        }

        if (model.getPaths() != null) {
            sparkParams.getPaths().clear();
            sparkParams.getPaths().addAll(model.getPaths());
        }

        if (model.getOptions() != null) {
            sparkParams.clearOptions();
            Map<String, String> encrypted = encryptSensitiveOptions(plugin, model.getOptions());

            for (Entry<String, String> entry : encrypted.entrySet()) {
                sparkParams.addOption(entry.getKey(), entry.getValue());
            }
        }

        return sparkParams;
    }

    public com.thinkbiganalytics.metadata.api.catalog.DataSource updateDataSource(DataSource model, com.thinkbiganalytics.metadata.api.catalog.DataSource domain) {
        domain.setTitle(model.getTitle());
        domain.setDescription(generateDescription(model));
        domain.setNifiControllerServiceId(model.getNifiControllerServiceId());
        updateSparkParameters(model.getConnector().getPluginId(), model.getTemplate(), domain.getSparkParameters());
        return domain;
    }
    
    public Function<DataSet, com.thinkbiganalytics.kylo.catalog.rest.model.DataSet> dataSetToRestModel() {
        return dataSetToRestModel(true);
    }

    public Function<DataSet, com.thinkbiganalytics.kylo.catalog.rest.model.DataSet> dataSetToRestModel(final boolean encryptedCredentials) {
        return (domain) -> {
            com.thinkbiganalytics.kylo.catalog.rest.model.DataSet model = new com.thinkbiganalytics.kylo.catalog.rest.model.DataSet();
            model.setId(domain.getId().toString());
            model.setDataSource(dataSourceToRestModel(true,encryptedCredentials).apply(domain.getDataSource()));
            model.setTitle(domain.getTitle());
            // TODO: add description
            DataSetTemplate template = sparkParamsToRestModel(domain.getDataSource().getConnector().getPluginId(), encryptedCredentials).apply(domain.getSparkParameters());
              if(!encryptedCredentials) {
                  decryptOptions(template);
              }
            model.setFormat(template.getFormat());
            model.setOptions(template.getOptions());
            model.setPaths(template.getPaths());
//            securityTransform.applyAccessControl(domain, model);
            return model;
        };
    }

    public Function<com.thinkbiganalytics.metadata.api.catalog.DataSource, DataSource> dataSourceToRestModel() {
        return dataSourceToRestModel(true, true);
    }

    public Function<com.thinkbiganalytics.metadata.api.catalog.DataSource, DataSource> dataSourceToRestModel(final boolean includeTemplate, final boolean encryptedCredentials) {
        return (domain) -> {
            DataSource model = new DataSource();
            model.setId(domain.getId().toString());
            model.setTitle(domain.getTitle());
            model.setNifiControllerServiceId(domain.getNifiControllerServiceId());
            model.setConnector(connectorToRestModel(includeTemplate, encryptedCredentials).apply(domain.getConnector()));
            securityTransform.applyAccessControl(domain, model);

            if (includeTemplate) {
                model.setTemplate(sparkParamsToRestModel(domain.getConnector().getPluginId(), encryptedCredentials).apply(domain.getSparkParameters()));
            }

            return model;
        };
    }

    /**
     * @return a domain to REST model converter for use by a Page
     */
    public Converter<com.thinkbiganalytics.metadata.api.catalog.DataSource, DataSource> convertDataSourceToRestModel(final boolean includeTemplate, final boolean encryptedCredentials) {
        return (domain) -> dataSourceToRestModel(includeTemplate, encryptedCredentials).apply(domain);
    }

    private String generateDescription(DataSource dataSource) {
        return "";
    }

    private String generateDescription(com.thinkbiganalytics.kylo.catalog.rest.model.DataSet dataSet) {
        return "";
    }
}
