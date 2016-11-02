package com.thinkbiganalytics.nifi.v2.metadata;

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
 * Created by sr186054 on 11/1/16.
 */
public class TriggerFeedTest {

    @Test
    public void testFeedDependencyResultsExecutionContext() {

        FeedDependencyDeltaResults deltaResults = new FeedDependencyDeltaResults();
        List<String> feedNames = new ArrayList();
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
