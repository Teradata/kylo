package com.thinkbiganalytics.nifi.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import org.apache.nifi.web.api.dto.DropRequestDTO;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AbstractNiFiConnectionsRestClientTest {

    /**
     * Verify deleting a queue.
     */
    @Test
    public void deleteQueueWithRetries() {
        // Mock drop requests
        final DropRequestDTO request1 = new DropRequestDTO();
        request1.setId("33d0b0c1-88b4-4c75-b61a-83223df79d8c");
        request1.setFinished(false);

        final DropRequestDTO request2 = new DropRequestDTO();
        request2.setId("33d0b0c1-88b4-4c75-b61a-83223df79d8c");
        request2.setFinished(true);

        // Mock NiFi Connections REST client
        final AbstractNiFiConnectionsRestClient client = Mockito.mock(AbstractNiFiConnectionsRestClient.class, Mockito.CALLS_REAL_METHODS);
        Mockito.when(client.createDropRequest("group", "connection")).thenReturn(request1);
        Mockito.when(client.getDropRequest("group", "connection", request1.getId())).thenReturn(Optional.of(request2));
        Mockito.when(client.deleteDropRequest("group", "connection", request1.getId())).thenReturn(Optional.of(request2));

        // Test deleting queue
        Assert.assertEquals(request2, client.deleteQueueWithRetries("group", "connection", 1, 0, TimeUnit.NANOSECONDS));
    }

    /**
     * Verify failure to delete due to timeout error.
     */
    @Test(expected = NifiClientRuntimeException.class)
    public void deleteQueueWithTimeout() {
        // Mock drop request
        final DropRequestDTO request = new DropRequestDTO();
        request.setId("33d0b0c1-88b4-4c75-b61a-83223df79d8c");
        request.setFinished(false);

        // Mock NiFi Connections REST client
        final AbstractNiFiConnectionsRestClient client = Mockito.mock(AbstractNiFiConnectionsRestClient.class, Mockito.CALLS_REAL_METHODS);
        Mockito.when(client.createDropRequest("group", "connection")).thenReturn(request);
        Mockito.when(client.getDropRequest("group", "connection", request.getId())).thenReturn(Optional.empty());
        Mockito.when(client.deleteDropRequest("group", "connection", request.getId())).thenReturn(Optional.empty());

        // Test failure
        client.deleteQueueWithRetries("group", "connection", 0, 0, TimeUnit.NANOSECONDS);
    }
}
