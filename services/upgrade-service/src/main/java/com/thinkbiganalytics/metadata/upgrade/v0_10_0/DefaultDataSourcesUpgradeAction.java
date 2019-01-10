package com.thinkbiganalytics.metadata.upgrade.v0_10_0;

/*-
 * #%L
 * kylo-upgrade-service
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

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager;
import com.thinkbiganalytics.kylo.catalog.spi.ConnectorPlugin;
import com.thinkbiganalytics.metadata.api.catalog.Connector;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;
import com.thinkbiganalytics.metadata.api.catalog.DataSource;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;


/**
 * This action creates the default data sources required by Kylo in 0.10.0.
 * <P>
 * Currently only the Hive data source is required.
 */
@Component("defaultDataSourcesUpgradeAction0.10.0")
@Profile(KyloUpgrader.KYLO_UPGRADE)
@Order(UpgradeAction.LATE_ORDER + 10)
public class DefaultDataSourcesUpgradeAction implements UpgradeAction {

    private static final Logger log = LoggerFactory.getLogger(DefaultDataSourcesUpgradeAction.class);
    
    @Value("${hive.datasource.driverClassName:#{null}}")
    private String driver;
    
    @Value("${hive.datasource.url:#{null}}")
    private String url;
    
    @Value("${hive.datasource.username:#{null}}")
    private String user;
    
    @Value("${hive.datasource.password:#{null}}")
    private String password;
    
    @Inject
    private ConnectorPluginManager pluginManager;
    
    @Inject 
    private ConnectorProvider connectorProvider;
    
    @Inject
    private DataSourceProvider dataSourceProvider;
    
    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.10", "0", "");
    }

    @Override
    public boolean isTargetFreshInstall(KyloVersion finalVersion) {
        return true;
    }

    @Override
    public void upgradeTo(final KyloVersion targetVersion) {
        log.info("Creating default catalog data sources: {}", targetVersion);
        
        Optional<ConnectorPlugin> plugin = pluginManager.getPlugin("hive");
        
        if (plugin.isPresent()) {
            Optional<Connector> connector = connectorProvider.findByPlugin(plugin.get().getId());
            
            if (connector.isPresent()) {
                List<DataSource> hiveSources = dataSourceProvider.findByConnector(connector.get().getId());
                
                // If at least one Hive data source exists then do nothing.
                if (hiveSources.size() == 0) {
                    log.info("Creating default Hive data source");
                    
                    DataSource ds = dataSourceProvider.create(connector.get().getId(), "Hive");
                    ds.setDescription("The default Hive data source");
                    
                    DataSetSparkParameters params = ds.getSparkParameters();
                    params.setFormat("hive");
                    params.addOption("driver", this.driver);
                    params.addOption("url", this.url);
                    params.addOption("user", this.user);
                    params.addOption("password", this.password);
                } else {
                    log.info("One or more Hive data sources already found: {}", hiveSources.stream().map(DataSource::toString).collect(Collectors.toList()));
                }
            } else {
                log.warn("No Hive connector found - cannot create a default Hive data source");
            }
        } else {
            log.warn("No Hive connector plugin found - cannot create a default Hive data source");
        }
    }
}
