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

import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.integration.feed.FeedIT;

import org.apache.nifi.web.api.dto.PortDTO;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;

public class ImportConnectedReusableTemplateIT extends FeedIT {


    @Test
    public void testConnectedReusableFlow() throws Exception {
        URL resource = IntegrationTestBase.class.getResource("connecting_reusable_flow.xml");

        ImportTemplate template1 = importReusableFlowXmlTemplate(resource.getPath(),null);
        String template1ProcessGroupId = template1.getTemplateResults().getProcessGroupEntity().getId();


        //Now import a reusable flow that has additional output ports in it.
        //Kylo will prompt to connect this to another reusable flow

        URL resource2 = IntegrationTestBase.class.getResource("reusable-flow1.xml");

        ImportTemplate template2 = importReusableFlowXmlTemplate(resource2.getPath(),null);
        //verify it failed
        Assert.assertFalse(template2.isSuccess());
        Assert.assertTrue(template2.isReusableFlowOutputPortConnectionsNeeded());



        //reassign the connections to connect to template1

       ReusableTemplateConnectionInfo connectionInfo = template2.getReusableTemplateConnections().stream().filter(conn -> "to another flow".equalsIgnoreCase(conn.getFeedOutputPortName())).findFirst().orElse(null);

       //Obtain the reusable connection input ports

        //get v1/feedmgr/nifi/reusable-input-ports
        PortDTO[] reusableInputPorts = getReusableInputPorts();

        //the 'connecting_reusable_flow.xml' has an input port named 'from reusable port'.
        //find it and try to connect it to this one
        PortDTO connectingPort = Arrays.stream(reusableInputPorts).filter(portDTO -> portDTO.getName().equalsIgnoreCase("from reusable port")).findFirst().orElse(null);
        if(connectingPort != null){
            connectionInfo.setInputPortDisplayName(connectingPort.getName());
            connectionInfo.setReusableTemplateInputPortName(connectingPort.getName());
            connectionInfo.setReusableTemplateProcessGroupName(template1.getTemplateResults().getProcessGroupEntity().getName());
        }
         template2 = importReusableFlowXmlTemplate(resource2.getPath(),connectionInfo);
        Assert.assertTrue(template2.isSuccess());
        Assert.assertFalse(template2.isReusableFlowOutputPortConnectionsNeeded());

    }
    }
