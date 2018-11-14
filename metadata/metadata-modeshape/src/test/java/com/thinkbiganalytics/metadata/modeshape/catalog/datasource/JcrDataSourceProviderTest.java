/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.catalog.datasource;

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
import com.thinkbiganalytics.metadata.api.catalog.Connector;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSource;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

/**
 *
 */
@SpringBootTest(classes = {ModeShapeEngineConfig.class, JcrTestConfig.class, CatalogMetadataConfig.class, JcrDataSourceProviderTest.TestConfig.class})
public class JcrDataSourceProviderTest extends AbstractTestNGSpringContextTests {

    private static final int COUNT = 10;
    
    @Inject
    private JcrMetadataAccess metadata;

    @Inject
    private ConnectorProvider connProvider;

    @Inject
    private DataSourceProvider dsProvider;
    
    private Connector.ID connId;
    private DataSource.ID dsId;


    @Configuration
    public static class TestConfig {
        @Bean(name="repositoryConfiguationResource")
        @Primary
        public Resource repositoryConfigurationResource() {
            return new ClassPathResource("/JcrDataSourceProviderTest-metadata-repository.json");
        }
        
        @Bean
        @Primary
        public ConnectorPluginManager mockConnectorPluginManager() {
            return Mockito.mock(ConnectorPluginManager.class);
        }
    }

    @BeforeClass
    public void setup() {
        this.connId = metadata.commit(() -> {
            Connector conn = this.connProvider.create("plugin1", "test1" + "_conn");
            return conn.getId();
        }, MetadataAccess.SERVICE);
    }
    
    
    @Test
    public void testCreate() {
        this.dsId = metadata.commit(() -> {
            DataSource ds = null;
            for (int i = 1; i <= COUNT + 1; i++) {
                ds = createDataSource(i);
            }
            return ds.getId();
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            Optional<DataSource> ds = this.dsProvider.find(this.dsId);
            
            assertThat(ds).isNotNull().isPresent();
            assertThat(ds.get()).extracting("systemName", "title", "description").contains(dsTuple(COUNT + 1).toArray());
            assertThat(ds.get().getConnector()).isNotNull().extracting("id").contains(this.connId);
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testDelete() {
        metadata.commit(() -> {
            this.dsProvider.deleteById(this.dsId);
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            Optional<DataSource> ds = this.dsProvider.find(this.dsId);
            
            assertThat(ds).isNotNull().isNotPresent();
        }, MetadataAccess.SERVICE);
    }
    
    @Test(dependsOnMethods="testDelete")
    public void testFindAll() {
        metadata.read(() -> {
            List<DataSource> conns = this.dsProvider.findAll();
            
            assertThat(conns).isNotNull();
            assertThat(conns).hasSize(COUNT);
        }, MetadataAccess.SERVICE);
    }
    
    @Test(dependsOnMethods="testDelete")
    public void testFindPage() {
        metadata.read(() -> {
            Page<DataSource> conns = this.dsProvider.findPage(new PageRequest(0, 5), null);
            
            assertThat(conns).isNotNull();
            assertThat(conns).hasSize(5);
            assertThat(conns).extracting("systemName", "title", "description").contains(dsTuple(1), dsTuple(2), dsTuple(3), dsTuple(4), dsTuple(5));
        }, MetadataAccess.SERVICE);
    }
    
    @Test(dependsOnMethods="testDelete")
    public void testFindByConnector() {
        metadata.read(() -> {
            List<DataSource> conns = this.dsProvider.findByConnector(this.connId);
            
            assertThat(conns).isNotNull();
            assertThat(conns).hasSize(COUNT);
        }, MetadataAccess.SERVICE);
    }
    
    @Test(dependsOnMethods = "testCreate")
    public void testConnectorHasDataSources() {
        metadata.read(() -> {

            Optional<Connector> conn = this.connProvider.find(this.connId);
            
            assertThat(conn).isNotNull().isPresent();
            
            List<? extends DataSource> dsList = conn.get().getDataSources();
            
            assertThat(dsList).hasSize(COUNT);
        }, MetadataAccess.SERVICE);
    }

    
    protected DataSource createDataSource(int tag) {
        DataSource ds = this.dsProvider.create(this.connId, "Test " + tag + " Data Source");
        ds.setDescription("Test description " + tag);
        return ds;
    }
    
    protected Tuple dsTuple(int tag) {
        String title = "Test " + tag + " Data Source";
        String sysName = title.replaceAll("\\s+", "_").toLowerCase();
        return tuple(sysName, title, "Test description " + tag);
    }
}
