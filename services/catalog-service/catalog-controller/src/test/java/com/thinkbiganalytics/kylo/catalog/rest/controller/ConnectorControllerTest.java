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

import com.thinkbiganalytics.kylo.catalog.connector.ConnectorProvider;
import com.thinkbiganalytics.kylo.catalog.rest.model.Connector;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.support.StaticMessageSource;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

public class ConnectorControllerTest {

    /**
     * Verify retrieving a connector.
     */
    @Test
    public void getConnector() {
        // Mock connector provider
        final ConnectorProvider provider = Mockito.mock(ConnectorProvider.class);

        final Connector connector = new Connector();
        connector.setId("C1");
        Mockito.when(provider.findConnector("C1")).thenReturn(Optional.of(connector));

        // Test retrieving connector
        final ConnectorController controller = newConnectionController();
        controller.connectorProvider = provider;

        final Response response = controller.getConnector("C1");
        Assert.assertEquals(connector, response.getEntity());
    }

    /**
     * Verify exception for missing connector.
     */
    @Test(expected = NotFoundException.class)
    public void getConnectorForMissing() {
        // Mock connector provider
        final ConnectorProvider provider = Mockito.mock(ConnectorProvider.class);
        Mockito.when(provider.findConnector(Mockito.anyString())).thenReturn(Optional.empty());

        // Test retrieving connector
        final ConnectorController controller = newConnectionController();
        controller.connectorProvider = provider;
        controller.getConnector("C1");
    }

    /**
     * Creates a new connection controller.
     */
    @Nonnull
    private ConnectorController newConnectionController() {
        final ConnectorController controller = new ConnectorController();
        controller.connectorProvider = Mockito.mock(ConnectorProvider.class);
        controller.request = Mockito.mock(HttpServletRequest.class);

        final StaticMessageSource messages = new StaticMessageSource();
        messages.setUseCodeAsDefaultMessage(true);
        controller.messages = messages;

        return controller;
    }
}
