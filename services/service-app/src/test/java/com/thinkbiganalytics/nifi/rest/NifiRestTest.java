package com.thinkbiganalytics.nifi.rest;

/*-
 * #%L
 * thinkbig-service-app
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

import com.thinkbiganalytics.feedmgr.nifi.CreateFeedBuilder;
import com.thinkbiganalytics.feedmgr.nifi.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.nifi.feedmgr.InputOutputPort;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessorSchedule;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.nifi.v1.rest.client.NiFiRestClientV1;
import com.thinkbiganalytics.nifi.v1.rest.model.NiFiPropertyDescriptorTransformV1;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ReportingTaskDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 */
public class NifiRestTest {


    private LegacyNifiRestClient restClient;
    private NifiFlowCache nifiFlowCache;
    private NiFiPropertyDescriptorTransformV1 propertyDescriptorTransform;

    @Before
    public void setupRestClient() {
        restClient = new LegacyNifiRestClient();
        NifiRestClientConfig clientConfig = new NifiRestClientConfig();
        clientConfig.setHost("localhost");
        clientConfig.setPort(8079);
        NiFiRestClient c = new NiFiRestClientV1(clientConfig);
        restClient.setClient(c);
        nifiFlowCache = new NifiFlowCache();
        propertyDescriptorTransform = new NiFiPropertyDescriptorTransformV1();

    }


    //@Test
    public void testCreateFeed1() {
        TemplateDTO templateDTO = restClient.getTemplateByName("New Data Ingest");
        String inputType = "org.apache.nifi.processors.standard.GetFile";

        NifiProcessorSchedule schedule = new NifiProcessorSchedule();
        schedule.setSchedulingStrategy("TIMER_DRIVEN");
        schedule.setSchedulingPeriod("10 sec");
        String inputPortName = "From Data Ingest Feed";

        String feedOutputPortName = "To Data Ingest";

        FeedMetadata feedMetadata = new FeedMetadata();
        feedMetadata.setCategory(new FeedCategory());
        feedMetadata.getCategory().setSystemName("online");
        feedMetadata.setSystemFeedName("Scotts Feed");

        CreateFeedBuilder.newFeed(restClient, nifiFlowCache, feedMetadata, templateDTO.getId(), new PropertyExpressionResolver(), propertyDescriptorTransform).inputProcessorType(inputType)
            .feedSchedule(schedule).addInputOutputPort(new InputOutputPort(inputPortName, feedOutputPortName)).build();
    }


    //@Test
    public void testLoad() {
        //setup constants for the test
        String templateName = "Data Ingest";
        int num = 10;
        String processGroupName = "LoadTest";
        String feedPrefix = "LT_";
        String inputType = "org.apache.nifi.processors.standard.GetFile";
        List<NifiProperty> templateProperties = new ArrayList<>();

        String schedulePeriod = "10 sec";

        String GET_FILE_PROCESSOR_NAME = "Poll filesystem";
        String UPDATE_PARAMETERS_PROCESSOR_NAME = "Update flow parameters";

        String INPUT_DIRECTORY_PROPERTY = "Input Directory";
        String SOURCE_PROPERTY = "source";
        String ENTITY_PROPERTY = "entity";

        try {
            TemplateDTO template = restClient.getTemplateByName(templateName);

            List<NifiProperty> propertyList = restClient.getPropertiesForTemplate(template.getId(),true);
            NifiProperty inputDirectory = NifiPropertyUtil
                .getProperty(GET_FILE_PROCESSOR_NAME, INPUT_DIRECTORY_PROPERTY, propertyList);
            NifiProperty entity = NifiPropertyUtil.getProperty(UPDATE_PARAMETERS_PROCESSOR_NAME, SOURCE_PROPERTY, propertyList);
            NifiProperty source = NifiPropertyUtil.getProperty(UPDATE_PARAMETERS_PROCESSOR_NAME, ENTITY_PROPERTY, propertyList);
            templateProperties.add(inputDirectory);
            templateProperties.add(entity);
            templateProperties.add(source);

            NifiProcessorSchedule schedule = new NifiProcessorSchedule();
            schedule.setSchedulingStrategy("TIMER_DRIVEN");
            schedule.setSchedulingPeriod(schedulePeriod);
            for (int i = 0; i < num; i++) {
                String feedName = feedPrefix + i;

                List<NifiProperty> instanceProperties = NifiPropertyUtil.copyProperties(templateProperties);
                //update the properties
                NifiPropertyUtil.getProperty(GET_FILE_PROCESSOR_NAME, INPUT_DIRECTORY_PROPERTY, instanceProperties).setValue("/tmp/" + feedName);
                NifiPropertyUtil.getProperty(UPDATE_PARAMETERS_PROCESSOR_NAME, SOURCE_PROPERTY, instanceProperties).setValue(processGroupName);
                NifiPropertyUtil.getProperty(UPDATE_PARAMETERS_PROCESSOR_NAME, ENTITY_PROPERTY, instanceProperties).setValue(feedName);

                FeedMetadata feedMetadata = new FeedMetadata();
                feedMetadata.setCategory(new FeedCategory());
                feedMetadata.getCategory().setSystemName(processGroupName);
                feedMetadata.setSystemFeedName("feedPrefix + i");

                CreateFeedBuilder.newFeed(restClient, nifiFlowCache, feedMetadata, template.getId(), new PropertyExpressionResolver(), propertyDescriptorTransform).inputProcessorType(inputType)
                    .feedSchedule(schedule).properties(instanceProperties).build();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //@Test
    public void testCreateFeed() throws Exception {
        TemplateDTO templateDTO = restClient.getTemplateByName("New Data Ingest");
        String inputType = "org.apache.nifi.processors.standard.GetFile";

        NifiProcessorSchedule schedule = new NifiProcessorSchedule();
        schedule.setSchedulingStrategy("TIMER_DRIVEN");
        schedule.setSchedulingPeriod("10 sec");
        String inputPortName = "From Data Ingest Feed";

        String feedOutputPortName = "To Data Ingest";

        FeedMetadata feedMetadata = new FeedMetadata();
        feedMetadata.setCategory(new FeedCategory());
        feedMetadata.getCategory().setSystemName("online");
        feedMetadata.setSystemFeedName("Scotts Feed");

        CreateFeedBuilder.newFeed(restClient, nifiFlowCache, feedMetadata, templateDTO.getId(), new PropertyExpressionResolver(), propertyDescriptorTransform).inputProcessorType(inputType)
            .feedSchedule(schedule).addInputOutputPort(new InputOutputPort(inputPortName, feedOutputPortName)).build();
    }


    //   @Test
    public void testOrder() throws Exception {

      /*  NifiVisitableProcessGroup g = restClient.getFlowOrder("27ab143a-0159-1000-4f6a-30f3746a341e", null);
        NifiFlowProcessGroup flow = new NifiFlowBuilder().build(g);

        NifiFlowProcessGroup flow2 = restClient.getFeedFlow("27ab143a-0159-1000-4f6a-30f3746a341e");

        List<String> feeds = Lists.newArrayList();
        feeds.add("sample.new_feed_three");
        List<NifiFlowProcessGroup> flows3 = restClient.getNiFiRestClient().flows().getFeedFlows(feeds);
*/
        List<NifiFlowProcessGroup> feedFlows = restClient.getFeedFlows();
        int i = 0;

    }


    // @Test
    public void testReportingTask() {
        LegacyNifiRestClient nifiRestClient = restClient;
        if (!nifiRestClient.getNiFiRestClient().reportingTasks().findFirstByType(NifiFlowCache.NiFiKyloProvenanceEventReportingTaskType).isPresent()) {
            //create it
            //1 ensure the controller service exists and is wired correctly
            Optional<ControllerServiceDTO> controllerService = nifiRestClient.getNiFiRestClient().reportingTasks().findFirstControllerServiceByType(NifiFlowCache.NiFiMetadataControllerServiceType);
            ControllerServiceDTO metadataService = null;
            if (controllerService.isPresent()) {
                metadataService = controllerService.get();
            } else {
                //create it and enable it
                //first create it
                ControllerServiceDTO controllerServiceDTO = new ControllerServiceDTO();
                controllerServiceDTO.setType(NifiFlowCache.NiFiMetadataControllerServiceType);
                controllerServiceDTO.setName("Kylo Metadata Service");
                metadataService = nifiRestClient.getNiFiRestClient().reportingTasks().createReportingTaskControllerService(controllerServiceDTO);
                //find the properties to inject
                Map<String, String> stringConfigProperties = new HashMap<>();
                metadataService = nifiRestClient.enableControllerServiceAndSetProperties(metadataService.getId(), stringConfigProperties);
            }

            if (metadataService != null) {
                if (NifiProcessUtil.SERVICE_STATE.DISABLED.name().equalsIgnoreCase(metadataService.getState())) {
                    //enable it....
                    metadataService = nifiRestClient.enableControllerServiceAndSetProperties(metadataService.getId(), null);
                }

                //assign the service to the reporting task

                ReportingTaskDTO reportingTaskDTO = new ReportingTaskDTO();
                reportingTaskDTO.setType(NifiFlowCache.NiFiKyloProvenanceEventReportingTaskType);
                reportingTaskDTO = nifiRestClient.getNiFiRestClient().reportingTasks().createReportingTask(reportingTaskDTO);
                //now set the properties
                ReportingTaskDTO updatedReportingTask = new ReportingTaskDTO();
                updatedReportingTask.setType(NifiFlowCache.NiFiKyloProvenanceEventReportingTaskType);
                updatedReportingTask.setId(reportingTaskDTO.getId());
                updatedReportingTask.setName("KyloProvenanceEventReportingTask");
                updatedReportingTask.setProperties(new HashMap<>(1));
                updatedReportingTask.getProperties().put("Metadata Service", metadataService.getId());
                updatedReportingTask.setSchedulingStrategy("TIMER_DRIVEN");
                updatedReportingTask.setSchedulingPeriod("5 secs");
                updatedReportingTask.setComments("Reporting task that will query the provenance repository and send the events and summary statistics over to Kylo via a JMS queue");
                updatedReportingTask.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());

                reportingTaskDTO = nifiRestClient.getNiFiRestClient().reportingTasks().update(updatedReportingTask);
            }

        }
        ;
    }

    // @Test
    public void testUpdateProcessor() {
        NifiProperty p = new NifiProperty();
        p.setProcessGroupId("0b013850-d6bb-44e4-87c2-1784858e60ab");
        p.setProcessorId("795509d5-1433-4e64-b7bd-d05c6adfb95a");
        p.setKey("Source Database Connection");
        p.setValue("4688ee71-262c-46bc-af35-9e9825507160");
        restClient.updateProcessorProperty(p.getProcessGroupId(), p.getProcessorId(), p);
        int i = 0;
    }

    // @Test
    public void testFile() throws IOException {
        InputStream in = NifiRestTest.class
            .getResourceAsStream("/template.xml");
        StringWriter writer = new StringWriter();
        IOUtils.copy(in, writer, "UTF-8");
        String theString = writer.toString();

        restClient.importTemplate("test", theString);
    }

}
