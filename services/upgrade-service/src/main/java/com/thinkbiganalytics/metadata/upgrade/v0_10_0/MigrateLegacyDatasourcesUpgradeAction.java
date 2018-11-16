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
import com.thinkbiganalytics.feedmgr.nifi.NifiControllerServiceProperties;
import com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager;
import com.thinkbiganalytics.metadata.api.catalog.Connector;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.catalog.JcrDataSetSparkParameters;
import com.thinkbiganalytics.metadata.modeshape.catalog.dataset.JcrDataSet;
import com.thinkbiganalytics.metadata.modeshape.catalog.dataset.JcrDataSetProvider;
import com.thinkbiganalytics.metadata.modeshape.catalog.datasource.JcrDataSource;
import com.thinkbiganalytics.metadata.modeshape.catalog.datasource.JcrDataSourceProvider;
import com.thinkbiganalytics.metadata.modeshape.common.MetadataPaths;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.IconableMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.PropertiedMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.SystemEntityMixin;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDerivedDatasource;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrJdbcDatasourceDetails;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrUserDatasource;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedSource;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedEntityActionsProvider;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeAction;
import com.thinkbiganalytics.server.upgrade.UpgradeException;

import org.apache.commons.lang3.StringUtils;
import org.modeshape.jcr.api.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;


/**
 * This action defines the permissions and roles introduce for the catalog in Kylo v0.10.0.
 */
@Component("migrateLegacyDatasourcesUpgradeAction0.10.0")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class MigrateLegacyDatasourcesUpgradeAction implements UpgradeAction {

    private static final Logger log = LoggerFactory.getLogger(MigrateLegacyDatasourcesUpgradeAction.class);
    
    @Inject
    private DatasourceProvider legacyProvider;
    
    @Inject
    private ConnectorPluginManager pluginManager;
    
    @Inject
    private FeedProvider feedProvider;
    
    @Inject
    private ConnectorProvider connectorProvider;
    
    @Inject
    private JcrDataSourceProvider dataSourceProvider;
    
    @Inject
    private JcrDataSetProvider dataSetProvider;

    @Inject
    private JcrAllowedEntityActionsProvider actionsProvider;

    @Inject
    private SecurityRoleProvider roleProvider;
    
    @Inject
    private AccessController accessController;
    
    @Inject 
    private NifiControllerServiceProperties controllerServiceProperties;
   
    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.10", "0", "");
    }

    @Override
    public void upgradeTo(final KyloVersion targetVersion) {
        log.info("Setting up catalog access control: {}", targetVersion);
        
        try {
            NodeTypeManager typeMgr = JcrMetadataAccess.getActiveSession().getWorkspace().getNodeTypeManager();
            NodeType dataSourceType = typeMgr.getNodeType("tba:DataSource");
            
            Connector conn = pluginManager.getPlugin("jdbc")
                .flatMap(plugin -> connectorProvider.findByPlugin(plugin.getId()))
                .orElseThrow(() -> new IllegalStateException("No JDBC connector found"));
                
            
            legacyProvider.getDatasources().stream()
                .map(JcrDatasource.class::cast)
                .map(JcrDatasource::getNode)
                .filter(node -> JcrUtil.isNodeType(node, "tba:userDatasource"))
                .filter(node -> JcrUtil.isNodeType(JcrUtil.getNode(node, "tba:details"), "tba:jdbcDatasourceDetails"))
                .forEach(jdbcNode -> {
                    try {
                        // Move the legacy datasource node to a temporary parent that doesn't have any constraints (the catalog folder).
                        Node tmpParent = JcrUtil.getNode(JcrUtil.getRootNode(jdbcNode), "metadata/catalog");
                        String dsSystemName = dataSourceProvider.generateSystemName(JcrUtil.getName(jdbcNode));
                        Path catDsPath = MetadataPaths.dataSourcePath(conn.getSystemName(), dsSystemName);
                        
                        Node catDsNode = JcrUtil.moveNode(jdbcNode, JcrUtil.path(tmpParent.getPath()).resolve(dsSystemName));
                        
                        // Wrap the node as a legacy UserDataSource and collect its properties.
                        JcrUserDatasource legacyDs = JcrUtil.getJcrObject(catDsNode, JcrUserDatasource.class);
                        Set<? extends JcrFeed> referencingFeeds = legacyDs.getFeedSources().stream()
                                .map(FeedSource::getFeed)
                                .map(JcrFeed.class::cast)
                                .collect(Collectors.toSet());
                        AtomicReference<String> controllerServiceId = new AtomicReference<>();
                        AtomicReference<String> password = new AtomicReference<>();
                        
                        legacyDs.getDetails()
                            .filter(JcrJdbcDatasourceDetails.class::isInstance)
                            .map(JcrJdbcDatasourceDetails.class::cast)
                            .ifPresent(details -> {
                                controllerServiceId.set(details.getControllerServiceId().orElse(null));
                                password.set("{cipher}" + details.getPassword());
                            });
                        
                        if (this.accessController.isEntityAccessControlled()) {
                            legacyDs.disableAccessControl(legacyDs.getOwner());
                        }
                        
                        // Convert the legacy type into the catalog type.
                        JcrDataSource catDs = convertToDataSource(catDsNode, catDsPath, dataSourceType, controllerServiceId.get(), password.get());
                        linkDataSets(catDs, referencingFeeds);
                        
                        if (this.accessController.isEntityAccessControlled()) {
                            List<SecurityRole> roles = roleProvider.getEntityRoles(SecurityRole.DATASOURCE);
                            actionsProvider.getAvailableActions(AllowedActions.DATASOURCE)
                                .ifPresent(actions -> catDs.enableAccessControl((JcrAllowedActions) actions, legacyDs.getOwner(), roles));
                        }
                    } catch (RepositoryException e) {
                        throw new UpgradeException("Failed to migrate legacy datasources", e);
                    }
                });
        } catch (IllegalStateException | RepositoryException e) {
            throw new UpgradeException("Failed to migrate legacy datasources", e);
        }
    }

    private JcrDataSource convertToDataSource(Node catDsNode, Path targetPath, NodeType dataSourceType, String controllerServiceId, String password) {
        return this.controllerServiceProperties.parseControllerService(controllerServiceId)
            .map(props -> {
                try {
                    NodeType[] legacyMixins = catDsNode.getMixinNodeTypes();
                    NodeType[] catMixins = Arrays.stream(dataSourceType.getDeclaredSupertypes())
                                    .filter(NodeType::isMixin)
                                    .toArray(NodeType[]::new);
                    // Strip off legacy nodes/properties
                    JcrPropertyUtil.setProperty(catDsNode, IconableMixin.ICON, null);
                    JcrPropertyUtil.setProperty(catDsNode, IconableMixin.ICON_COLOR, null);
                    JcrPropertyUtil.setProperty(catDsNode, SystemEntityMixin.SYSTEM_NAME, null);
                    JcrPropertyUtil.setProperty(catDsNode, JcrUserDatasource.SOURCE_NAME, null);
                    JcrPropertyUtil.setProperty(catDsNode, JcrUserDatasource.TYPE, null);
                    JcrUtil.removeNode(catDsNode, PropertiedMixin.PROPERTIES_NAME);
                    JcrUtil.removeNode(catDsNode, JcrUserDatasource.DETAILS);
                    JcrUtil.removeMixins(catDsNode, legacyMixins);
                    
                    catDsNode.setPrimaryType("tba:DataSource");
                    JcrUtil.addMixins(catDsNode, catMixins);
                    JcrUtil.getOrCreateNode(catDsNode, "tba:sparkParams", "tba:DataSetSparkParams");
                    
                    JcrDataSource catDs = JcrUtil.getJcrObject(catDsNode, JcrDataSource.class);
                    JcrDataSetSparkParameters sparkParameters = (JcrDataSetSparkParameters)catDs.getSparkParameters();
                    
                    catDs.setNifiControllerServiceId(controllerServiceId);
                    sparkParameters.addOption("password", password);
                    sparkParameters.setFormat("jdbc");
                    if(StringUtils.isNotBlank(props.getDriverLocation())){
                        Arrays.asList(props.getDriverLocation().split(",")).stream().forEach(jar ->  sparkParameters.addJar(jar));
                    }
                    sparkParameters.addOption("url", props.getUrl());
                    sparkParameters.addOption("driver", props.getDriverClassName());
                    sparkParameters.addOption("user", props.getUser());
                    
                    catDsNode.getSession().save();
                    
                    Node movedNode = JcrUtil.moveNode(catDsNode, targetPath);
                    return new JcrDataSource(movedNode);
                } catch (RepositoryException e) {
                    throw new UpgradeException("Failed to migrate legacy datasources", e);
                }
            })
            .orElseThrow(() -> new UpgradeException("Unable to obtain controller service properties for service with ID {} - NiFi must be available during upgrade"));
    }

    private void linkDataSets(JcrDataSource catDs, Set<? extends JcrFeed> referencingFeeds) {
        referencingFeeds.stream()
            .forEach(feed -> {
                String title = catDs.getTitle();
                
                feed.getSources().stream()
                    .map(JcrFeedSource.class::cast)
                    .forEach(source -> {
                        source.getDatasource()
                            .map(JcrDatasource.class::cast)
                            .ifPresent(datasource -> {
                                // There will be a derived datasource of with a connection name matching the new data datasource
                                // for each table.  Create a data set from each table name.
                                if (datasource instanceof JcrDerivedDatasource) {
                                    JcrDerivedDatasource dds = (JcrDerivedDatasource) datasource;
                                    
                                    if (dds.getProperty("tba:datasourceType").equals("DatabaseDatasource") && title.equals(dds.getAllProperties().get("Database Connection"))) {
                                        Map<String, Object> allProps = dds.getAllProperties();
                                        
                                        if (allProps.containsKey("Table")) {
                                            String tableName = allProps.get("Table").toString();
                                            JcrDataSet dataSet = (JcrDataSet) dataSetProvider.build(catDs.getId())
                                                            .title(tableName)
                                                            .addOption("dbtable", tableName)
                                                            .build();
                                            
                                            feed.removeFeedSource(source);
                                            feedProvider.ensureFeedSource(feed.getId(), dataSet.getId());
                                        } else {
                                            log.warn("No table name found in data source: " + dds);
                                        }
                                   }
                                // Since we've converted a legacy datasource into a category data source with the same ID,
                                // there will still be a reference to it in one of the FeedSources as a legacy datasource.  
                                // When we find it then remove that FeedSource.
                                } else if (datasource.getNode().equals(catDs.getNode())) {
                                    feed.removeFeedSource(source);
                                }
                            });
                    });
            });
    }
}
