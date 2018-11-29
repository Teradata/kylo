package com.thinkbiganalytics.metadata.modeshape.catalog;

/*-
 * #%L
 * kylo-metadata-modeshape
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
import com.thinkbiganalytics.kylo.catalog.rest.model.ConnectorPluginDescriptor;
import com.thinkbiganalytics.kylo.catalog.spi.ConnectorPlugin;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.api.catalog.Connector;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorAlreadyExistsException;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSetProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.catalog.connector.JcrConnectorProvider;
import com.thinkbiganalytics.metadata.modeshape.catalog.dataset.JcrDataSetProvider;
import com.thinkbiganalytics.metadata.modeshape.catalog.datasource.JcrDataSourceProvider;

import org.apache.commons.lang3.StringUtils;
import org.modeshape.jcr.api.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.jcr.Session;

@Configuration
public class CatalogMetadataConfig {

    private static final Logger log = LoggerFactory.getLogger(CatalogMetadataConfig.class);

    @Bean
    public ConnectorProvider connectorProvider() {
        return new JcrConnectorProvider();
    }

    @Bean
    public DataSourceProvider dataSourceProvider() {
        return new JcrDataSourceProvider();
    }

    @Bean
    public DataSetProvider dataSetProvider() {
        return new JcrDataSetProvider();
    }

    @Bean
    public PostMetadataConfigAction connectorPluginSyncAction(ConnectorPluginManager pluginMgr, MetadataAccess metadata) {
        return () -> {
            try {
                doPluginSyncAction(pluginMgr, metadata);
            } catch (ConnectorAlreadyExistsException e) {
                log.warn("Encountered an error attempting to synchronize plugins", e);
                log.info("Rebuilding modeshape indexes to ensure they are accurately synchronized with the data.   Please be patient as this may take a long time!");

                try {
                    reindex(metadata);
                    doPluginSyncAction(pluginMgr, metadata);
                } catch (ConnectorAlreadyExistsException e2) {
                    log.error("Unrecoverable fatal exception. Encountered connector already exists after synchronizing modeshape indexes", e2);
                    throw e2;
                }
            }
        };
    }


    private void reindex(MetadataAccess metadata) {
        metadata.commit(() -> {
            Session session = JcrMetadataAccess.getActiveSession();
            Workspace workspace = (Workspace) session.getWorkspace();
            workspace.reindex();
        }, MetadataAccess.SERVICE);
    }


    private void doPluginSyncAction(ConnectorPluginManager pluginMgr, MetadataAccess metadata) {
        metadata.commit(() -> {
            List<ConnectorPlugin> plugins = pluginMgr.getPlugins();
            Map<String, Connector> connectorMap = connectorProvider().findAll(true).stream().collect(Collectors.toMap(Connector::getPluginId, c -> c));

            for (ConnectorPlugin plugin : plugins) {
                Connector connector = connectorMap.get(plugin.getId());
                ConnectorPluginDescriptor descr = plugin.getDescriptor();

                if (connector != null) {
                    connectorMap.get(plugin.getId()).setActive(true);
                    connectorMap.remove(plugin.getId());
                } else {
                    String title = descr.getTitle();
                    connector = connectorProvider().create(plugin.getId(), title);
                }

                connector.setIconColor(descr.getColor());
                connector.setIcon(descr.getIcon());
                if (StringUtils.isNotBlank(descr.getFormat())) {
                    connector.getSparkParameters().setFormat(descr.getFormat());
                }
            }

            // Any left over connectors that do not have a corresponding plugin should be either made inactive if they at
            // least one data source, or removed if they don't.
            for (Connector connector : connectorMap.values()) {
                if (connector.getDataSources().isEmpty()) {
                    connectorProvider().deleteById(connector.getId());
                } else {
                    connector.setActive(false);
                }
            }
        }, MetadataAccess.SERVICE);
    }
}
