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

import com.thinkbiganalytics.nifi.rest.model.NifiProcessorSchedule;

import org.apache.nifi.web.api.dto.ProcessorConfigDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by sr186054 on 8/25/17.
 */
public abstract class AbstractNiFiProcessorsRestClient implements NiFiProcessorsRestClient {

    private static final Logger log = LoggerFactory.getLogger(AbstractNiFiProcessorsRestClient.class);

    protected ProcessorDTO applySchedule(NifiProcessorSchedule schedule) {
        if (schedule != null && schedule.getProcessorId() != null) {
            ProcessorDTO input = new ProcessorDTO();
            input.setId(schedule.getProcessorId());
            input.setConfig(new ProcessorConfigDTO());
            input.getConfig().setSchedulingPeriod(schedule.getSchedulingPeriod());
            input.getConfig().setSchedulingStrategy(schedule.getSchedulingStrategy());
            input.getConfig().setConcurrentlySchedulableTaskCount(schedule.getConcurrentTasks());
            return input;
        }
        return null;
    }

    @Override
    public ProcessorDTO schedule(NifiProcessorSchedule schedule) {
        if (schedule != null && schedule.getProcessorId() != null) {
            ProcessorDTO input = applySchedule(schedule);
            if (input != null) {
                log.info("About to update the schedule for processor: {} to be {} with a value of: {} ", schedule.getProcessorId(), schedule.getSchedulingStrategy(), schedule.getSchedulingPeriod());
                return updateWithRetry(input, 5, 300, TimeUnit.MILLISECONDS);
            }
        }
        return null;
    }

}
