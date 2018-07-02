package com.thinkbiganalytics.integration.template;

/*-
 * #%L
 * kylo-service-app
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

import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;

import org.apache.nifi.web.api.dto.PortDTO;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;

public class ImportConnectedReusableTemplateIT extends IntegrationTestBase {

    public ConnectedTemplate registerConnectedReusableTemplate(){
        URL resource = IntegrationTestBase.class.getResource("connecting_reusable_flow.xml");

        ImportTemplate template1 = importReusableFlowXmlTemplate(resource.getPath(), null);
        String template1ProcessGroupId = template1.getTemplateResults().getProcessGroupEntity().getId();

        //Now import a reusable flow that has additional output ports in it.
        //Kylo will prompt to connect this to another reusable flow

        URL resource2 = IntegrationTestBase.class.getResource("reusable-flow1.xml");

        ImportTemplate template2 = importReusableFlowXmlTemplate(resource2.getPath(), null);
        //verify it failed
        Assert.assertFalse(template2.isSuccess());
        Assert.assertTrue(template2.isReusableFlowOutputPortConnectionsNeeded());

        //reassign the connections to connect to template1

        ReusableTemplateConnectionInfo
            connectionInfo =
            template2.getReusableTemplateConnections().stream().filter(conn -> "to another flow".equalsIgnoreCase(conn.getFeedOutputPortName())).findFirst().orElse(null);

        //Obtain the reusable connection input ports

        //get v1/feedmgr/nifi/reusable-input-ports
        PortDTO[] reusableInputPorts = getReusableInputPorts();

        //the 'connecting_reusable_flow.xml' has an input port named 'from reusable port'.
        //find it and try to connect it to this one
        PortDTO connectingPort = Arrays.stream(reusableInputPorts).filter(portDTO -> portDTO.getName().equalsIgnoreCase("from reusable port")).findFirst().orElse(null);
        if (connectingPort != null) {
            connectionInfo.setInputPortDisplayName(connectingPort.getName());
            connectionInfo.setReusableTemplateInputPortName(connectingPort.getName());
            connectionInfo.setReusableTemplateProcessGroupName(template1.getTemplateResults().getProcessGroupEntity().getName());
        }
        template2 = importReusableFlowXmlTemplate(resource2.getPath(), connectionInfo);
        Assert.assertTrue(template2.isSuccess());
        Assert.assertFalse(template2.isReusableFlowOutputPortConnectionsNeeded());

        //get the flow for the parent processor and verify it is connected to the other reusable flow
        NifiFlowProcessGroup flow = getFlow(template2.getTemplateResults().getProcessGroupEntity().getId());
        Assert.assertNotNull(flow);
        boolean testUpdate2ProcessorExists = flow.getProcessorMap().values().stream().anyMatch(p -> "test-update2".equalsIgnoreCase(p.getName()));
        Assert.assertTrue(testUpdate2ProcessorExists);
        return new ConnectedTemplate(template1,template2);

    }

    public static class ConnectedTemplate {
        ImportTemplate template1;

        ImportTemplate template2;

        public ConnectedTemplate(ImportTemplate template1, ImportTemplate template2) {
            this.template1 = template1;
            this.template2 = template2;
        }

        public ImportTemplate getTemplate1() {
            return template1;
        }

        public ImportTemplate getTemplate2() {
            return template2;
        }
    }


    /**
     *  - Imports a reusable template (template1)
     *  - Imports another reusable template (template2) with an output ports
     *  - connects template2 to template 1
     *  - verifies the connection is made correctly
     *
     * @throws Exception
     */
    @Test
    public void testConnectedReusableFlow() throws Exception {
        registerConnectedReusableTemplate();




    }
}
