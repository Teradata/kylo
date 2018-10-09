/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.catalog.dataset;

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
import com.thinkbiganalytics.metadata.api.catalog.DataSet;
import com.thinkbiganalytics.metadata.api.catalog.DataSetProvider;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

/**
 *
 */
@SpringBootTest(classes = {ModeShapeEngineConfig.class, JcrTestConfig.class, CatalogMetadataConfig.class, JcrDataSetProviderTest.TestConfig.class})
public class JcrDataSetProviderTest extends AbstractTestNGSpringContextTests {

    private static final int COUNT = 5;
    
    @Inject
    private JcrMetadataAccess metadata;

    @Inject
    private ConnectorProvider connProvider;

    @Inject
    private DataSourceProvider dataSourceProvider;

    @Inject
    private DataSetProvider dataSetProvider;
    
    private List<DataSet.ID> dSetIds = new ArrayList<>();
    private List<DataSource.ID> dSrcIds = new ArrayList<>();

    @Configuration
    public static class TestConfig {
        @Bean(name="repositoryConfiguationResource")
        @Primary
        public Resource repositoryConfigurationResource() {
            return new ClassPathResource("/JcrDataSetProviderTest-metadata-repository.json");
        }
        
        @Bean
        @Primary
        public ConnectorPluginManager mockConnectorPluginManager() {
            return Mockito.mock(ConnectorPluginManager.class);
        }
    }

    @BeforeClass
    public void setup() {
        metadata.commit(() -> {
            Connector conn = this.connProvider.create("plugin1", "test1" + "_conn");
            this.dSrcIds.add(this.dataSourceProvider.create(conn.getId(), "dataset0").getId());
            this.dSrcIds.add(this.dataSourceProvider.create(conn.getId(), "dataset1").getId());
        }, MetadataAccess.SERVICE);
    }
    
    
    @Test
    public void testCreate() {
        metadata.commit(() -> {
            DataSet dset = null;
            for (int srcIdx = 0; srcIdx < 2; srcIdx++) {
                for (int dsIdx = 1; dsIdx <= COUNT + 1; dsIdx++) {
                    dset = createDataSet(this.dSrcIds.get(srcIdx), srcIdx * 10 + dsIdx);
                }
                
                this.dSetIds.add(dset.getId());
            }
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            Optional<DataSet> dset0 = this.dataSetProvider.find(this.dSetIds.get(0));
            
            assertThat(dset0).isNotNull().isPresent();
            assertThat(dset0.get()).extracting("title", "description").contains(dataSetTuple(COUNT + 1).toArray());
            assertThat(dset0.get().getDataSource()).isNotNull().extracting("id").contains(this.dSrcIds.get(0));
            
            Optional<DataSet> dset1 = this.dataSetProvider.find(this.dSetIds.get(1));
            
            assertThat(dset1).isNotNull().isPresent();
            assertThat(dset1.get()).extracting("title", "description").contains(dataSetTuple(10 + COUNT + 1).toArray());
            assertThat(dset1.get().getDataSource()).isNotNull().extracting("id").contains(this.dSrcIds.get(1));
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testDelete() {
        metadata.commit(() -> {
            this.dataSetProvider.deleteById(this.dSetIds.get(0));
            this.dataSetProvider.deleteById(this.dSetIds.get(1));
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            Optional<DataSet> ds0 = this.dataSetProvider.find(this.dSetIds.get(0));
            Optional<DataSet> ds1 = this.dataSetProvider.find(this.dSetIds.get(1));
            
            assertThat(ds0).isNotNull().isNotPresent();
            assertThat(ds1).isNotNull().isNotPresent();
        }, MetadataAccess.SERVICE);
    }
    
    @Test(dependsOnMethods="testDelete")
    public void testFindAll() {
        metadata.read(() -> {
            List<DataSet> conns = this.dataSetProvider.findAll();
            
            assertThat(conns).isNotNull();
            assertThat(conns).hasSize(COUNT * 2);
        }, MetadataAccess.SERVICE);
    }
    
    @Test(dependsOnMethods="testDelete")
    public void testFindPage() {
        metadata.read(() -> {
            Page<DataSet> conns = this.dataSetProvider.findPage(new PageRequest(0, 5), null);
            
            assertThat(conns).isNotNull();
            assertThat(conns).hasSize(5);
            assertThat(conns).extracting("title", "description").contains(dataSetTuple(1), dataSetTuple(2), dataSetTuple(3), dataSetTuple(4), dataSetTuple(5));
        }, MetadataAccess.SERVICE);
    }
    
    @Test(dependsOnMethods="testDelete")
    public void testFindByConnector() {
        metadata.read(() -> {
            List<DataSet> conns = this.dataSetProvider.findByDataSource(this.dSrcIds.get(0), this.dSrcIds.get(1));
            
            assertThat(conns).isNotNull();
            assertThat(conns).hasSize(COUNT * 2);
        }, MetadataAccess.SERVICE);
    }
    
    @Test(dependsOnMethods="testDelete")
    public void testDataSourceHasDataSets() {
        metadata.read(() -> {
            Optional<DataSource> dsrc = this.dataSourceProvider.find(dSrcIds.get(0));
            
            assertThat(dsrc).isNotNull().isPresent();
            
            List<? extends DataSet> dsList = dsrc.get().getDataSets();
            
            assertThat(dsList).hasSize(COUNT);
        }, MetadataAccess.SERVICE);
    }

    
    protected DataSet createDataSet(DataSource.ID srcId, int tag) {
        return this.dataSetProvider.build(srcId)
            .title("Test " + tag + " Data Set")
            .description("Test description " + tag)
            .build();
    }
    
    protected Tuple dataSetTuple(int tag) {
        String title = "Test " + tag + " Data Set";
        return tuple(title, "Test description " + tag);
    }
}
