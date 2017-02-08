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

import com.google.common.collect.ImmutableSet;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ClientErrorException;

public class AbstractNiFiProcessGroupsRestClientTest {

    /**
     * Verify deleting a process group.
     */
    @Test
    public void deleteWithRetries() {
        // Mock process group
        final ProcessGroupDTO group = new ProcessGroupDTO();
        group.setId("d526adec-1f33-463b-8570-e9cf3e6c8703");
        group.setParentGroupId("93b1abbb-f805-4f52-8a9e-ffdab224dc44");

        // Mock NiFi Process Groups REST client
        final AbstractNiFiProcessGroupsRestClient client = Mockito.mock(AbstractNiFiProcessGroupsRestClient.class, Mockito.CALLS_REAL_METHODS);
        Mockito.when(client.doDelete(group)).thenThrow(new ClientErrorException(409)).thenReturn(Optional.empty());

        // Test delete
        Assert.assertEquals(Optional.empty(), client.deleteWithRetries(group, 1, 0, TimeUnit.NANOSECONDS));
    }

    /**
     * Verify failure to delete due to timeout error.
     */
    @Test(expected = NifiClientRuntimeException.class)
    public void deleteWithTimeout() {
        // Mock process group
        final ProcessGroupDTO group = new ProcessGroupDTO();
        group.setId("d526adec-1f33-463b-8570-e9cf3e6c8703");
        group.setParentGroupId("93b1abbb-f805-4f52-8a9e-ffdab224dc44");

        // Mock NiFi Process Groups REST client
        final AbstractNiFiProcessGroupsRestClient client = Mockito.mock(AbstractNiFiProcessGroupsRestClient.class, Mockito.CALLS_REAL_METHODS);
        Mockito.when(client.doDelete(group)).thenThrow(new ClientErrorException(409)).thenReturn(Optional.empty());

        // Test failure
        client.deleteWithRetries(group, 0, 0, TimeUnit.NANOSECONDS);
    }

    /**
     * Verify finding a process group by name.
     */
    @Test
    public void findByName() {
        // Mock process groups
        final ProcessGroupDTO group1 = new ProcessGroupDTO();
        group1.setId("b179527b-0a7a-4ba8-9817-60f7a9ffb9d5");
        group1.setName("group1");

        final ProcessGroupDTO group2 = new ProcessGroupDTO();
        group2.setId("7ba659c2-6ddd-4215-8709-1eafff04851d");
        group2.setName("group2");

        final ProcessGroupDTO group3 = new ProcessGroupDTO();
        group3.setId("2dbcc8ce-a0e7-455c-9e3d-f4f82d08acc3");
        group3.setName("group3");

        final ProcessGroupDTO fullGroup2 = new ProcessGroupDTO();
        fullGroup2.setId("7ba659c2-6ddd-4215-8709-1eafff04851d");
        fullGroup2.setName("group2");
        fullGroup2.setRunningCount(8);
        fullGroup2.setStoppedCount(0);

        // Mock NiFi Process Groups REST client
        final NiFiProcessGroupsRestClient client = Mockito.mock(AbstractNiFiProcessGroupsRestClient.class, Mockito.CALLS_REAL_METHODS);
        Mockito.when(client.findAll("parent")).thenReturn(ImmutableSet.of(group1, group2, group3));
        Mockito.when(client.findById(group2.getId(), true, true)).thenReturn(Optional.of(fullGroup2));

        // Test finding process group by name
        Assert.assertEquals(group2, client.findByName("parent", "group2", false, false).get());
        Assert.assertEquals(fullGroup2, client.findByName("parent", "group2", true, true).get());

        Assert.assertFalse(client.findByName("parent", "invalid", false, false).isPresent());
        Assert.assertFalse(client.findByName("parent", "invalid", true, true).isPresent());
    }
}
