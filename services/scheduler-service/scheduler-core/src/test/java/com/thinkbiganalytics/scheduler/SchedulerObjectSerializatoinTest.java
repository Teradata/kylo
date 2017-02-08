package com.thinkbiganalytics.scheduler;

/*-
 * #%L
 * thinkbig-scheduler-core
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
import com.thinkbiganalytics.scheduler.model.DefaultJobIdentifier;
import com.thinkbiganalytics.scheduler.model.DefaultTriggerIdentifier;
import com.thinkbiganalytics.scheduler.model.DefaultTriggerInfo;

import org.junit.Test;

import java.io.IOException;

/**
 */
public class SchedulerObjectSerializatoinTest {

    @Test
    public void testTriggerInfoSerialization() {

        try {
            TriggerInfo info = new DefaultTriggerInfo(new DefaultJobIdentifier(), new DefaultTriggerIdentifier());
            ObjectMapper objectMapper = new ObjectMapper();

            String json = null;
            json = objectMapper.writeValueAsString(info);
            System.out.println(json);

            TriggerInfo info2 = objectMapper.readValue(json, TriggerInfo.class);
            System.out.println(info2);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
