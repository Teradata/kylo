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

import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.Set;

public class AbstractNiFiTemplatesRestClientTest {

    /**
     * Verifies finding templates by an input port name
     */
    @Test
    public void findByInputPortName() {
        // Mock templates with only basic info
        final TemplateDTO basicTemplate1 = new TemplateDTO();
        basicTemplate1.setId("cf54ca27-ccb0-49a1-94f2-2834c4379b70");

        final TemplateDTO basicTemplate2 = new TemplateDTO();
        basicTemplate2.setId("43ce4a07-10ac-40c0-b195-598bc753988b");

        final TemplateDTO basicTemplate3 = new TemplateDTO();
        basicTemplate3.setId("7f57c685-f9dd-497e-8e97-7d1e6d43b59c");

        // Mock templates with port info
        final PortDTO port1 = new PortDTO();
        port1.setId("ee77fc99-2f19-43d3-ae26-091f9caa1401");
        port1.setName("to-other-template");

        final PortDTO port2 = new PortDTO();
        port2.setId("1b2a3c92-1964-4f87-9835-b8a19f024249");
        port2.setName("to-standard-ingest");

        final FlowSnippetDTO flow1 = new FlowSnippetDTO();
        flow1.setInputPorts(ImmutableSet.of(port1));

        final FlowSnippetDTO flow2 = new FlowSnippetDTO();
        flow2.setInputPorts(ImmutableSet.of(port2));

        final TemplateDTO fullTemplate1 = new TemplateDTO();
        fullTemplate1.setId("cf54ca27-ccb0-49a1-94f2-2834c4379b70");
        fullTemplate1.setSnippet(flow1);

        final TemplateDTO fullTemplate2 = new TemplateDTO();
        fullTemplate2.setId("43ce4a07-10ac-40c0-b195-598bc753988b");
        fullTemplate2.setSnippet(flow2);

        // Mock the NiFi Templates REST client
        final NiFiTemplatesRestClient client = Mockito.mock(AbstractNiFiTemplatesRestClient.class, Mockito.CALLS_REAL_METHODS);
        Mockito.when(client.findAll()).thenReturn(ImmutableSet.of(basicTemplate1, basicTemplate2, basicTemplate3));
        Mockito.when(client.findById("cf54ca27-ccb0-49a1-94f2-2834c4379b70")).thenReturn(Optional.of(fullTemplate1));
        Mockito.when(client.findById("43ce4a07-10ac-40c0-b195-598bc753988b")).thenReturn(Optional.of(fullTemplate2));
        Mockito.when(client.findById("7f57c685-f9dd-497e-8e97-7d1e6d43b59c")).thenReturn(Optional.empty());

        // Test finding matching templates
        final Set<TemplateDTO> matches = client.findByInputPortName("to-standard-ingest");
        Assert.assertEquals(fullTemplate2, matches.stream().findFirst().get());

        Assert.assertEquals(0, client.findByInputPortName("invalid").size());
    }

    /**
     * Verifies finding templates by matching the template name
     */
    @Test
    public void findByName() {
        // Mock templates
        final TemplateDTO template1 = new TemplateDTO();
        template1.setName("temp1");

        final TemplateDTO template2 = new TemplateDTO();
        template2.setName("temp2");

        final TemplateDTO template3 = new TemplateDTO();
        template3.setName("temp3");

        // Mock the NiFi Templates REST client
        final NiFiTemplatesRestClient client = Mockito.mock(AbstractNiFiTemplatesRestClient.class, Mockito.CALLS_REAL_METHODS);
        Mockito.when(client.findAll()).thenReturn(ImmutableSet.of(template1, template2, template3));

        // Test finding matching template
        Assert.assertEquals(template2, client.findByName("temp2").get());
        Assert.assertFalse(client.findByName("invalid").isPresent());
    }
}
