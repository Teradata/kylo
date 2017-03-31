package com.thinkbiganalytics.nifi.rest.client;

/*-
 * #%L
 * kylo-nifi-rest-client-api
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

import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AbstractNiFiControllerServicesRestClientTest {

    /**
     * Verify updating the state of a controller service.
     */
    @Test
    public void updateStateByIdWithRetries() {
        // Mock controller services
        final ControllerServiceDTO response1 = new ControllerServiceDTO();
        response1.setState("ENABLING");

        final ControllerServiceDTO response2 = new ControllerServiceDTO();
        response2.setState("ENABLED");

        // Mock NiFi Controller Service REST client
        final AbstractNiFiControllerServicesRestClient client = Mockito.mock(AbstractNiFiControllerServicesRestClient.class, Mockito.CALLS_REAL_METHODS);
        Mockito.when(client.update(Mockito.any())).thenReturn(response1);
        Mockito.when(client.findById(Mockito.anyString())).thenReturn(Optional.of(response2));

        // Test updating state
        Assert.assertEquals(response2, client.updateStateByIdWithRetries("MYID", "ENABLED", 1, 0, TimeUnit.NANOSECONDS));
    }

    /**
     * Verify exception when updating the state of a controller service times out.
     */
    @Test(expected = NifiClientRuntimeException.class)
    public void updateStateByIdWithTimeout() {
        // Mock NiFi REST client
        final NiFiRestClient nifiClient = Mockito.mock(NiFiRestClient.class);
        Mockito.when(nifiClient.getBulletins("MYID")).thenReturn(Collections.emptyList());

        // Mock controller services
        final ControllerServiceDTO response = new ControllerServiceDTO();
        response.setState("ENABLING");

        // Mock NiFi Controller Service REST client
        final AbstractNiFiControllerServicesRestClient client = Mockito.mock(AbstractNiFiControllerServicesRestClient.class, Mockito.CALLS_REAL_METHODS);
        Mockito.when(client.update(Mockito.any())).thenReturn(response);
        Mockito.when(client.findById(Mockito.anyString())).thenReturn(Optional.of(response));
        Mockito.when(client.getClient()).thenReturn(nifiClient);

        // Test updating state
        client.updateStateByIdWithRetries("MYID", "ENABLED", 1, 0, TimeUnit.NANOSECONDS);
    }
}
