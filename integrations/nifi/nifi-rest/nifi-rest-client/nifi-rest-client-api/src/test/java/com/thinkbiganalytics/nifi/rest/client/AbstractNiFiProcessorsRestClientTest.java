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

import com.thinkbiganalytics.nifi.rest.support.NifiFeedConstants;

import org.apache.nifi.web.api.dto.ProcessorConfigDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AbstractNiFiProcessorsRestClientTest {


    private ProcessorDTO newProcessor(String id, String state, String scheduleStrategy, String schedule) {
        final ProcessorDTO dto = new ProcessorDTO();
        dto.setId(id);
        dto.setParentGroupId("parent");
        dto.setConfig(new ProcessorConfigDTO());
        dto.getConfig().setSchedulingPeriod(schedule);
        dto.getConfig().setSchedulingStrategy(scheduleStrategy);
        dto.setState(state);
        return dto;
    }

    /**
     * Verify waking up a processor
     */
    @Test
    public void wakeUp() {

        List<ProcessorDTO> processors = new ArrayList<>();
        processors.add(newProcessor("cron-stopped", "STOPPED", NifiFeedConstants.SCHEDULE_STRATEGIES.CRON_DRIVEN.name(), "0 0 12 1/1 * ? *"));
        processors.add(newProcessor("cron-disabled", "DISABLED", NifiFeedConstants.SCHEDULE_STRATEGIES.CRON_DRIVEN.name(), "0 0 12 1/1 * ? *"));
        processors.add(newProcessor("cron-started", "RUNNING", NifiFeedConstants.SCHEDULE_STRATEGIES.CRON_DRIVEN.name(), "0 0 12 1/1 * ? *"));
        processors.add(newProcessor("timer-stopped", "STOPPED", NifiFeedConstants.SCHEDULE_STRATEGIES.TIMER_DRIVEN.name(), "5 min"));
        processors.add(newProcessor("timer-disabled", "DISABLED", NifiFeedConstants.SCHEDULE_STRATEGIES.TIMER_DRIVEN.name(), "5 min"));
        processors.add(newProcessor("timer-started", "RUNNING", NifiFeedConstants.SCHEDULE_STRATEGIES.TIMER_DRIVEN.name(), "5 min"));

        // Mock NiFi Process Groups REST client
        final NiFiProcessorsRestClient client = Mockito.mock(AbstractNiFiProcessorsRestClient.class, Mockito.CALLS_REAL_METHODS);
        Mockito.when(client.updateWithRetry(Mockito.any(ProcessorDTO.class), Mockito.anyInt(), Mockito.anyInt(), Mockito.any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ProcessorDTO processorDTO = invocationOnMock.getArgumentAt(0, ProcessorDTO.class);
                return processorDTO;
            }
        });

        processors.stream().forEach(processorDTO -> {
            Mockito.when(client.findById("parent", processorDTO.getId())).thenReturn(Optional.of(processorDTO));
            ProcessorDTO wakeupDto = client.wakeUp(processorDTO);

            if (wakeupDto.getConfig() != null) {
                //Ensure the dto after wakeup matches the starting dto
                Assert.assertEquals(wakeupDto.getConfig().getSchedulingStrategy(), processorDTO.getConfig().getSchedulingStrategy());
                Assert.assertEquals(wakeupDto.getConfig().getSchedulingPeriod(), processorDTO.getConfig().getSchedulingPeriod());

                if (wakeupDto.getState() != null) {
                    Assert.assertEquals(wakeupDto.getState(), processorDTO.getState());
                }
            }
        });


    }
}
