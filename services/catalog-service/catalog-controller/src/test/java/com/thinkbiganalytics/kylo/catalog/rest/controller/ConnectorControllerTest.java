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
import static org.mockito.Matchers.anyString;

import com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager;
import com.thinkbiganalytics.kylo.catalog.rest.model.CatalogModelTransform;
import com.thinkbiganalytics.metadata.MockMetadataAccess;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.catalog.Connector;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider;
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.MessageSource;

import java.util.Optional;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

public class ConnectorControllerTest {

    @Mock
    private ConnectorProvider provider;
    
    @Mock
    private MessageSource messages;
    
    @Mock
    private Connector.ID connectorId;
    
    @Spy
    private TestConnector connector;
    
    @Spy
    private TestConnectorPlugin connectorPlugin;
    
    @Spy
    private SecurityModelTransform securityTransform = Mockito.mock(SecurityModelTransform.class);
    
    @Spy
    private ConnectorPluginManager connectorPluginManager = Mockito.mock(ConnectorPluginManager.class);
    
    @Spy
    private MetadataAccess metadataService = new MockMetadataAccess();
    
    @Spy
    private CatalogModelTransform modelTransform = new CatalogModelTransform(this.securityTransform, this.connectorPluginManager, null);
    
    @InjectMocks
    private ConnectorController controller = new ConnectorController() {
        @Override
        protected String getMessage(String code, Object... args) { return code; };
        @Override
        protected String getMessage(String code) { return code; }
    };
    
    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(connectorPluginManager.getPlugin(anyString())).thenReturn(Optional.of(this.connectorPlugin));
        Mockito.when(provider.resolveId(Mockito.anyString())).thenReturn(connectorId);
        Mockito.when(connector.getId()).thenReturn(connectorId);
    }
    
    /**
     * Verify retrieving a connector.
     */
    @Test
    public void getConnector() {
        Mockito.when(provider.find(Mockito.any(Connector.ID.class))).thenReturn(Optional.of(connector));
        Mockito.when(connectorId.toString()).thenReturn("C1");

        final Response response = controller.getConnector("C1", true);

        assertThat(response.getEntity())
            .isNotNull()
            .extracting("id").contains("C1");
    }

    /**
     * Verify exception for missing connector.
     */
    @Test(expected = NotFoundException.class)
    public void getConnectorForMissing() {
        Mockito.when(provider.find(Mockito.any(Connector.ID.class))).thenReturn(Optional.empty());

        controller.getConnector("C1", true);
    }
}
