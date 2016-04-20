package com.thinkbiganalytics.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.TriggerIdentifier;
import com.thinkbiganalytics.scheduler.TriggerInfo;
import com.thinkbiganalytics.scheduler.impl.TriggerInfoImpl;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by sr186054 on 9/25/15.
 */
public class SchedulerObjectSerializatoinTest {

    @Test
    public void testTriggerInfoSerialization() {

        try {
            TriggerInfo info = new TriggerInfoImpl(new JobIdentifier(), new TriggerIdentifier());
            ObjectMapper objectMapper = new ObjectMapper();

            String json = null;
            json = objectMapper.writeValueAsString(info);
            System.out.println(json);

            TriggerInfo info2 = objectMapper.readValue(json,TriggerInfo.class);
            System.out.println(info2);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
