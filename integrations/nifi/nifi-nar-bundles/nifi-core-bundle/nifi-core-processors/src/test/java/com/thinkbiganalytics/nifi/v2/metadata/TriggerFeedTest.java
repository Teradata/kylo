package com.thinkbiganalytics.nifi.v2.metadata;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.thinkbiganalytics.metadata.api.op.FeedDependencyDeltaResults;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class TriggerFeedTest {

    @Test
    public void testFeedDependencyResultsExecutionContext() {

        FeedDependencyDeltaResults deltaResults = new FeedDependencyDeltaResults();
        List<String> feedNames = new ArrayList<>();
        feedNames.add("category.feed_a");
        feedNames.add("category.feed_b");

        deltaResults.setDependentFeedNames(feedNames);

        Map<String, FeedDependencyDeltaResults.FeedJobExecutionData> jobData = new HashMap<>();

        feedNames.stream().forEach(feedName -> {
            Map<String, Object> executionContext = new HashMap<>();
            executionContext.put("param1", "test");
            executionContext.put("export.kylo.param2", "test2");
            deltaResults.addFeedExecutionContext(feedName, new Long(1), DateTime.now(), DateTime.now(), executionContext);

        });

        String executionContextKeys = "export.kylo, export.test, test2 ";

        List<String> list = new ArrayList<String>(Arrays.asList(executionContextKeys.trim().split("\\s*,\\s*")));

        deltaResults.reduceExecutionContextToMatchingKeys(list);

        //assert just the 1 property got sent to the execution context
        Assert.assertEquals(1, deltaResults.getLatestFeedJobExecutionContext().get(feedNames.get(0)).getExecutionContext().size());

        //validate JSON transform

        ObjectMapper MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JodaModule());
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            String value = MAPPER.writeValueAsString(deltaResults);
            Assert.assertNotNull(value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


    }


}
