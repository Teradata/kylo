package com.thinkbiganalytics.kylo.catalog.rest.controller;

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

import static org.assertj.core.api.Assertions.assertThat;

import com.thinkbiganalytics.kylo.catalog.rest.model.CatalogModelTransform;
import com.thinkbiganalytics.metadata.MockMetadataAccess;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.catalog.Connector;
import com.thinkbiganalytics.metadata.api.catalog.DataSet;
import com.thinkbiganalytics.metadata.api.catalog.DataSetBuilder;
import com.thinkbiganalytics.metadata.api.catalog.DataSetProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSource;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceNotFoundException;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider;
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;

import java.util.Optional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

@Ignore
public class DataSetControllerTest {

    @Mock
    private DataSetProvider dataSetProvider;

    @Mock
    private DataSourceProvider dataSourceProvider;
    
    @Mock
    private MessageSource messages;
    
    @Mock
    private Connector.ID connectorId;
    
    @Spy
    private TestConnector connector;
    
    @Mock
    private DataSource.ID dataSourceId;
    
    @Spy
    private TestDataSource dataSource;
    
    @Mock
    private DataSet.ID dataSetId;

    @Spy
    private TestDataSet dataSet;
    
//    @Mock
    private DataSetBuilder dataSetBuilder;
    
    @Spy
    private MetadataAccess metadataService = new MockMetadataAccess();
    
    @Mock
    private SecurityModelTransform securityTransform = new SecurityModelTransform();
    
    @Spy
    private CatalogModelTransform modelTransform = new CatalogModelTransform();

    
    @InjectMocks
    private DataSetController controller = new DataSetController() {
        @Override
        protected String getMessage(String code, Object... args) { return code; };
        @Override
        protected String getMessage(String code) { return code; }
    };
    
    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        
        Mockito.when(connector.getId()).thenReturn(connectorId);

        Mockito.when(dataSourceProvider.resolveId(Mockito.anyString())).thenReturn(dataSourceId);
        Mockito.when(dataSource.getId()).thenReturn(dataSourceId);
        Mockito.when(dataSource.getConnector()).thenReturn(connector);
        
        this.dataSetBuilder = Mockito.mock(DataSetBuilder.class, 
                                           (Answer<?>) (invocation) -> invocation.getMethod().getReturnType().isInstance(invocation.getMock()) 
                                               ? invocation.getMock() 
                                               : Mockito.RETURNS_DEFAULTS.answer(invocation));
        
        Mockito.when(dataSetProvider.resolveId(Mockito.anyString())).thenReturn(dataSetId);
        Mockito.when(dataSetProvider.build(Mockito.any())).thenReturn(this.dataSetBuilder);
        Mockito.when(dataSet.getId()).thenReturn(dataSetId);
        Mockito.when(dataSet.getDataSource()).thenReturn(dataSource);
        
        this.modelTransform.setSecurityTransform(this.securityTransform);
    }
    
    
    /**
     * Verify creating a data set.
     */
    @Test
    public void createDataSet() {
        Mockito.when(dataSetBuilder.build()).thenReturn(dataSet);
        Mockito.when(dataSetId.toString()).thenReturn("dataSet1");
        
        final com.thinkbiganalytics.kylo.catalog.rest.model.DataSource src = modelTransform.dataSourceToRestModel().apply(dataSource);
        final Response response = controller.createDataSet(new com.thinkbiganalytics.kylo.catalog.rest.model.DataSet(src, "MySQL Test"));
        
        assertThat(response.getEntity())
            .isNotNull()
            .extracting("id", "format").contains("dataSet1", "jdbc");
    }

    /**
     * Verify exception for creating an invalid data set.
     */
    @Test(expected = BadRequestException.class)
    public void createDataSetWithInvalid() {
        Mockito.when(dataSetBuilder.build()).thenThrow(new DataSourceNotFoundException(null));
        
        final com.thinkbiganalytics.kylo.catalog.rest.model.DataSource src = modelTransform.dataSourceToRestModel().apply(dataSource);
        controller.createDataSet(new com.thinkbiganalytics.kylo.catalog.rest.model.DataSet(src, ""));
    }

    /**
     * Verify retrieving a data set.
     */
    @Test
    public void getDataSet() {
        Mockito.when(dataSetProvider.find(Mockito.any(DataSet.ID.class))).thenReturn(Optional.of(dataSet));
        Mockito.when(dataSetId.toString()).thenReturn("dataSet1");

        final Response response = controller.getDataSet("dataSet1");
        
        assertThat(response.getEntity())
            .isNotNull()
            .extracting("id", "format").contains("dataSet1", "jdbc");
    }

    /**
     * Verify exception for retrieving a missing data set.
     */
    @Test(expected = NotFoundException.class)
    public void getDataSetForMissing() {
        Mockito.when(dataSetProvider.find(Mockito.any(DataSet.ID.class))).thenReturn(Optional.empty());

        controller.getDataSet("id");
    }
}
