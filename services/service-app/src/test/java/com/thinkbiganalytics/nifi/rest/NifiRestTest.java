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
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.nifi.TemplateConnectionUtil;
import com.thinkbiganalytics.feedmgr.nifi.cache.DefaultNiFiFlowCompletionCallback;
import com.thinkbiganalytics.feedmgr.nifi.cache.NiFiFlowInspectorManager;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCacheImpl;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.nifi.feedmgr.InputOutputPort;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessorSchedule;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiFlowBuilder;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.nifi.v1.rest.client.NiFiRestClientV1;
import com.thinkbiganalytics.nifi.v1.rest.model.NiFiPropertyDescriptorTransformV1;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class NifiRestTest {

    private static final Logger log = LoggerFactory.getLogger(NifiRestTest.class);
    private NiFiObjectCache createFeedBuilderCache;
    private LegacyNifiRestClient restClient;
    private NifiFlowCache nifiFlowCache;
    private NiFiPropertyDescriptorTransformV1 propertyDescriptorTransform;
    private TemplateConnectionUtil templateConnectionUtil;

    @Before
    public void setupRestClient() {
        restClient = new LegacyNifiRestClient();
        NifiRestClientConfig clientConfig = new NifiRestClientConfig();
        //clientConfig.setHost("localhost");
        clientConfig.setHost("34.208.236.190");
        clientConfig.setPort(8079);
        NiFiRestClient c = new NiFiRestClientV1(clientConfig);
        restClient.setClient(c);
        nifiFlowCache = new NifiFlowCacheImpl();
        propertyDescriptorTransform = new NiFiPropertyDescriptorTransformV1();
        createFeedBuilderCache = new NiFiObjectCache();
        createFeedBuilderCache.setRestClient(restClient);
        templateConnectionUtil = new TemplateConnectionUtil();
        templateConnectionUtil.setRestClient(restClient);
        templateConnectionUtil.setNifiFlowCache(nifiFlowCache);
        templateConnectionUtil.setNiFiObjectCache(createFeedBuilderCache);
        templateConnectionUtil.setPropertyDescriptorTransform(propertyDescriptorTransform);


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

        CreateFeedBuilder.newFeed(restClient, nifiFlowCache, feedMetadata, templateDTO.getId(), new PropertyExpressionResolver(), propertyDescriptorTransform, createFeedBuilderCache, templateConnectionUtil)
            .inputProcessorType(inputType)
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

            List<NifiProperty> propertyList = restClient.getPropertiesForTemplate(template.getId(), true);
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

                CreateFeedBuilder.newFeed(restClient, nifiFlowCache, feedMetadata, template.getId(), new PropertyExpressionResolver(), propertyDescriptorTransform, createFeedBuilderCache,templateConnectionUtil)
                    .inputProcessorType(inputType)
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

        CreateFeedBuilder.newFeed(restClient, nifiFlowCache, feedMetadata, templateDTO.getId(), new PropertyExpressionResolver(), propertyDescriptorTransform, createFeedBuilderCache, templateConnectionUtil)
            .inputProcessorType(inputType)
            .feedSchedule(schedule).addInputOutputPort(new InputOutputPort(inputPortName, feedOutputPortName)).build();
    }

    //@Test
    public void testInspection() {
        DefaultNiFiFlowCompletionCallback completionCallback = new DefaultNiFiFlowCompletionCallback();
        NiFiFlowInspectorManager flowInspectorManager = new NiFiFlowInspectorManager.NiFiFlowInspectorManagerBuilder(restClient.getNiFiRestClient())
            .startingProcessGroupId("root")
            .completionCallback(completionCallback)
            .threads(50)
            .waitUntilComplete(true)
            .buildAndInspect();

        log.info("NiFi Flow Inspection took {} ms with {} threads for {} feeds, {} processors and {} connections ", flowInspectorManager.getTotalTime(), flowInspectorManager.getThreadCount(),completionCallback.getFeedNames().size(),
                 completionCallback.getProcessorIdToProcessorName().size(), completionCallback.getConnectionIdCacheNameMap().size());

        int i = 0;
    }

    // @Test
    public void testOrder() throws Exception {

        NifiVisitableProcessGroup g = restClient.getFlowOrder("63de0732-015e-1000-f198-dcd76ac2942e", null);
        NifiFlowProcessGroup flow = new NifiFlowBuilder().build(g);

        // NifiFlowProcessGroup flow2 = restClient.getFeedFlow("27ab143a-0159-1000-4f6a-30f3746a341e");

        // List<String> feeds = Lists.newArrayList();
        //   feeds.add("sample.new_feed_three");
        //   List<NifiFlowProcessGroup> flows3 = restClient.getNiFiRestClient().flows().getFeedFlows(feeds);

//        List<NifiFlowProcessGroup> feedFlows = restClient.getFeedFlows();
        int i = 0;

    }

    //  @Test
    public void findFeedProcessGroup() throws Exception {
        String feedName = "cat_62_feed_282";
        String categoryGroupId = "66e65266-015e-1000-703b-cb619274da55";
        long start = System.currentTimeMillis();
        ProcessGroupDTO feedGroup = restClient.getProcessGroupByName(categoryGroupId, feedName);
        long stop = System.currentTimeMillis();
        long time = stop - start;
        int i = 0;
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
