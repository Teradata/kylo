/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.catalog.connector;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.api.catalog.Connector;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.catalog.CatalogMetadataConfig;

import org.assertj.core.groups.Tuple;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

/**
 *
 */
@SpringBootTest(classes = {ModeShapeEngineConfig.class, JcrTestConfig.class, CatalogMetadataConfig.class, JcrConnectorProviderTest.TestConfig.class})
public class JcrConnectorProviderTest extends AbstractTestNGSpringContextTests {

    private static final int COUNT = 10;
    
    @Inject
    private JcrMetadataAccess metadata;

    @Inject
    private ConnectorProvider connectorProvider;
    
    private Connector.ID testId;


    @Configuration
    public static class TestConfig {
        @Bean(name="repositoryConfiguationResource")
        @Primary
        public Resource repositoryConfigurationResource() {
            return new ClassPathResource("/JcrConnectorProviderTest-metadata-repository.json");
        }
        
        @Bean
        @Primary
        public ConnectorPluginManager mockConnectorPluginManager() {
            return Mockito.mock(ConnectorPluginManager.class);
        }
        
        @Bean
        @Primary
        public PostMetadataConfigAction connectorPluginSyncAction(ConnectorPluginManager pluginMgr, MetadataAccess metadata) {
            return Mockito.mock(PostMetadataConfigAction.class);
        }
    }

    
    @Test
    public void testCreate() {
        this.testId = metadata.commit(() -> {
            Connector conn = null;
            
            for (int i = 1; i <= COUNT + 1; i++) {
                conn = createConnector(Integer.toString(i));
            }
            
            return conn.getId();
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            Optional<Connector> conn = this.connectorProvider.find(this.testId);
            
            assertThat(conn).isNotNull().isPresent();
            assertThat(conn.get()).extracting("pluginId", "systemName", "title", "description").contains(connectorTuple(COUNT + 1).toArray());
        }, MetadataAccess.SERVICE);
    }

    @Test(dependsOnMethods="testCreate")
    public void testDelete() {
        metadata.commit(() -> {
            this.connectorProvider.deleteById(this.testId);
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            Optional<Connector> conn = this.connectorProvider.find(this.testId);
            
            assertThat(conn).isNotNull().isNotPresent();
        }, MetadataAccess.SERVICE);
    }
    
    @Test(dependsOnMethods="testDelete")
    public void testFindByPlugin() {
        metadata.read(() -> {
            Optional<Connector> conn = this.connectorProvider.findByPlugin("plugin1");
            
            assertThat(conn).isNotNull().isPresent();
            assertThat(conn.get()).extracting("pluginId", "systemName", "title", "description").contains("plugin1", "test_1_connector", "Test 1 Connector", "Test description 1");
        }, MetadataAccess.SERVICE);
    }
    
    @Test(dependsOnMethods="testDelete")
    public void testFindAll() {
        metadata.read(() -> {
            List<Connector> conns = this.connectorProvider.findAll(true);
            
            assertThat(conns).isNotNull();
            assertThat(conns).hasSize(COUNT);
        }, MetadataAccess.SERVICE);
    }
    
    @Test(dependsOnMethods="testDelete")
    public void testFindPage() {
        metadata.read(() -> {
            Page<Connector> conns = this.connectorProvider.findPage(new PageRequest(0, 5), null);
            
            assertThat(conns).isNotNull();
            assertThat(conns).hasSize(5);
            assertThat(conns).extracting("pluginId", "systemName", "title", "description").contains(connectorTuple("1"),
                                                                                                    connectorTuple("2"),
                                                                                                    connectorTuple("3"),
                                                                                                    connectorTuple("4"),
                                                                                                    connectorTuple("5"));
        }, MetadataAccess.SERVICE);
    }

    
    
    protected Connector createConnector(int tag) {
        return createConnector(Integer.toString(tag));
    }
    
    protected Connector createConnector(String tag) {
        Connector conn1 = this.connectorProvider.create("plugin" + tag, "Test " + tag + " Connector");
        conn1.setDescription("Test description " + tag);
        return conn1;
    }
    
    protected Tuple connectorTuple(int tag) {
        return connectorTuple(Integer.toString(tag));
    }
    
    protected Tuple connectorTuple(String tag) {
        String title = "Test " + tag + " Connector";
        String sysName = title.replaceAll("\\s+", "_").toLowerCase();
        return tuple("plugin" + tag, sysName, title, "Test description " + tag);
    }
}
