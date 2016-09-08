package com.thinkbiganalytics.nifi.rest;/*
 * Copyright (c) 2016.
 */

import com.thinkbiganalytics.nifi.feedmgr.CreateFeedBuilder;
import com.thinkbiganalytics.nifi.feedmgr.InputOutputPort;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessorSchedule;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiFlowBuilder;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessor;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.PropertyDescriptorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.dto.provenance.ProvenanceRequestDTO;
import org.apache.nifi.web.api.entity.BulletinBoardEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.ProcessorEntity;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 1/11/16.
 */
public class NifiRestTest {


    private NifiRestClient restClient;

    @Before
    public void setupRestClient() {
        NifiRestClientConfig config = new NifiRestClientConfig();
        config.setHost("localhost");
        config.setPort(8079);
        restClient = new NifiRestClient(config);
    }


    //@Test
    public void testGetTemplate() {

        try {
            restClient.getTemplates(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void testGetProperties() {
        try {
            List<NifiProperty> propertyList = restClient.getAllProperties();
            for (NifiProperty property : propertyList) {
                System.out.println(property.getKey() + " = " + property.getValue() + " as  " + property.getPropertyDescriptor().getDescription());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // @Test
    public void testFile() throws IOException, Exception {
        InputStream in = NifiRestTest.class
            .getResourceAsStream("/template.xml");
        StringWriter writer = new StringWriter();
        IOUtils.copy(in, writer, "UTF-8");
        String theString = writer.toString();

        restClient.importTemplate("test", theString);
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

            List<NifiProperty> propertyList = restClient.getPropertiesForTemplate(template.getId());
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

                restClient.createTemplateInstanceAsProcessGroup(template.getId(), processGroupName, feedPrefix + i, inputType, instanceProperties, schedule);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void testUpdateProperties() {
        try {
            List<NifiProperty> propertyList = restClient.getAllProperties();
            for (NifiProperty property : propertyList) {
                System.out.println(property.getKey() + " = " + property.getValue() + " as  " + property.getPropertyDescriptor().getDescription());
                if (property.getProcessorName().equalsIgnoreCase("GetFile") && property.getKey().equalsIgnoreCase("Input Directory")) {
                    property.setValue("/scott-changed");
                }
            }
            restClient.updateProcessGroupProperties(propertyList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void testBulletins() {
        String groupId = "root";
        try {
            BulletinBoardEntity entity = restClient.getBulletins(null);
            int i = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // @Test
    public void testDeleteRootGroups() {
        String groupId = "root";
        try {
            restClient.deleteChildProcessGroups(groupId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // @Test
    public void testGetProcessorsForTemplate() {
        String templateId = "5ae589b3-72d8-483c-884a-8eab79a00dbb";
        try {
            Collection<ProcessorDTO> processors = restClient.getProcessorsForTemplate(templateId, false);
            for (ProcessorDTO dto : processors) {
                System.out.println(dto.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //@Test
    public void getProcessor() {

        try {
            ProcessorEntity
                entity = restClient.getProcessor("9e75099f-d689-4d57-a113-dbe43fef0af5", "58b5da24-8f14-452f-b8cb-8e6a052a629e");
            ProcessorDTO dto = entity.getProcessor();
            int i = 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // @Test
    public void getControllerServices() {
        try {
            ControllerServicesEntity servicesEntity = restClient.getControllerServices(null);
            for (ControllerServiceDTO dto : servicesEntity.getControllerServices()) {
                for (Map.Entry<String, String> property : dto.getProperties().entrySet()) {
                    String key = property.getKey();
                    PropertyDescriptorDTO descriptorDTO = dto.getDescriptors().get(key);
                    NifiProperty nifiProperty = new NifiProperty();
                    nifiProperty.setKey(key);
                    nifiProperty.setPropertyDescriptor(descriptorDTO);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testOrder() throws Exception {
        NifiVisitableProcessGroup g = restClient.getFlowOrder("7f836b40-e79d-4964-8cb7-0bd34264998d");
        NifiFlowProcessGroup flow = new NifiFlowBuilder().build(g);

        //   Set<ProcessorDTO> processors = restClient.getProcessorsForFlow("c4c7c4be-5421-45a0-87e9-fdef211297c5");

        Set<ProcessorDTO> failureProcessors = new HashSet<>();
        for (NifiVisitableProcessor p : g.getStartingProcessors()) {
            failureProcessors.addAll(p.getFailureProcessors());
        }

        for (ProcessorDTO p : failureProcessors) {
            System.out.println("FAILURE " + p.getName() + ", " + p.getId());
        }


    }

    //@Test
    public void testFailureProcessors() throws Exception {

        Set<ProcessorDTO> failureProcessors = restClient.getFailureProcessors("9a9035fd-2341-46f6-b7c3-e61724a722d4");
        for (ProcessorDTO p : failureProcessors) {
            System.out.println("FAILURE " + p.getName() + ", " + p.getId());
        }
    }

    //@Test
    public void testFlowOrderProcessors() throws Exception {
        Set<ProcessorDTO> processors = restClient.getProcessorsForFlow("e434dd00-1d80-4f67-ac3b-38df5dfcd7f2");
        for (ProcessorDTO p : processors) {
            System.out.println("Processor " + p.getName() + ", " + p.getId());
        }
    }

    //@Test
    public void testCreateGlobalTemplateConnection() throws Exception {

        String feedGroupId = "e434dd00-1d80-4f67-ac3b-38df5dfcd7f2";
        String feedCategoryId = "85201c5d-4484-49ed-aafe-3af461361af9";
        String reusableTemplateCategoryGroupId = "3613b9ad-07d3-4045-b0b4-c4996476c3a2";
        String templateGroupId = "9a9035fd-2341-46f6-b7c3-e61724a722d4";
        String inputPortName = "From Data Ingest Feed";
        String feedOutputPortName = "To Data Ingest";
        restClient.connectFeedToGlobalTemplate(feedGroupId, feedOutputPortName, feedCategoryId, reusableTemplateCategoryGroupId, inputPortName);

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
        CreateFeedBuilder.newFeed(restClient, "online", "Scotts Feed", templateDTO.getId()).inputProcessorType(inputType)
            .feedSchedule(schedule).addInputOutputPort(new InputOutputPort(inputPortName, feedOutputPortName)).build();
    }

    //@Test
    public void testRegisterReusableTemplate() throws Exception {

        String categoryId = "3613b9ad-07d3-4045-b0b4-c4996476c3a2";
        String templateId = "cd6143ed-c75e-46e2-b934-487fd86891e6";
        String inputPortName = "From Data Ingest Feed";
        restClient.createReusableTemplateInputPort(categoryId, templateId, inputPortName);

    }

    //   @Test
    public void testLineage() throws Exception {

        ProvenanceRequestDTO request = new ProvenanceRequestDTO();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -3);
        request.setStartDate(cal.getTime());
        request.setEndDate(new Date());
        request.setMaxResults(10000);

    }


    // @Test
    public void stopProcessors() throws Exception {
        restClient.stopAllProcessors("946a5a63-97e7-4004-ad18-f55ef44eecfe", "0a0729d0-5c15-47ff-843c-e4eb3a691370");
    }


}
