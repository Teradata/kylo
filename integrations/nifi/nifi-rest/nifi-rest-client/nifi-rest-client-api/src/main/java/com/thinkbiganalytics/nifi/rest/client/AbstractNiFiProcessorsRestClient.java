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
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiFeedConstants.SCHEDULE_STRATEGIES;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil.PROCESS_STATE;

import org.apache.nifi.web.api.dto.ProcessorConfigDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created by sr186054 on 8/25/17.
 */
public abstract class AbstractNiFiProcessorsRestClient implements NiFiProcessorsRestClient {

    private static final Logger log = LoggerFactory.getLogger(AbstractNiFiProcessorsRestClient.class);

    private static final String TRIGGER_TIMER_PERIOD = "24 hrs";

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

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.rest.client.NiFiProcessorsRestClient#wake(org.apache.nifi.web.api.dto.ProcessorDTO)
     */
    @Override
    public ProcessorDTO wakeUp(ProcessorDTO processor) {
        return findById(processor.getParentGroupId(), processor.getId())
            .map(initState -> {
                ProcessorDTO current = initState;
                List<Function<ProcessorDTO, ProcessorDTO>> wakeSequence = generateWakeSequence(current);
                
                for (Function<ProcessorDTO, ProcessorDTO> transition : wakeSequence) {
                    current = transition.apply(createProcessor(current));
                }
                
                return current;
            })
            .orElseThrow(() -> new NifiComponentNotFoundException(processor.getId(), NifiConstants.NIFI_COMPONENT_TYPE.PROCESSOR, null));
    }

    private List<Function<ProcessorDTO, ProcessorDTO>> generateWakeSequence(ProcessorDTO originalProc) {
        List<Function<ProcessorDTO, ProcessorDTO>> sequence = new ArrayList<>();
        PROCESS_STATE state = PROCESS_STATE.valueOf(originalProc.getState().toUpperCase());
        SCHEDULE_STRATEGIES strategy = SCHEDULE_STRATEGIES.valueOf(originalProc.getConfig().getSchedulingStrategy().toUpperCase());
        
        // Stop the processor if it is disabled or running.
        if (state == PROCESS_STATE.DISABLED || state == PROCESS_STATE.RUNNING) {
            sequence.add((proc) -> {
                proc.setState(PROCESS_STATE.STOPPED.name());
                return updateWithRetry(proc, 5, 300, TimeUnit.MILLISECONDS);
            });
        }
        
        // If the processor is was already running and it was timer-driven then there is no reason to change its 
        // configuration; just restarting will trigger a run.
        // In all other cases add the sequence: set a long timer -> start -> stop -> reset original scheduling.
        if (state != PROCESS_STATE.RUNNING || strategy != SCHEDULE_STRATEGIES.TIMER_DRIVEN) {
            // Set long timer -> start -> stop -> reset scheduling.
            sequence.add((proc) -> {
                proc.setConfig(createConfig(SCHEDULE_STRATEGIES.TIMER_DRIVEN.name(), TRIGGER_TIMER_PERIOD));
                proc.setState(PROCESS_STATE.RUNNING.name());
                return updateWithRetry(proc, 5, 300, TimeUnit.MILLISECONDS);
            });

            sequence.add(proc -> {
                delay(2000);
                proc.setState(PROCESS_STATE.STOPPED.name());
                return updateWithRetry(proc, 5, 300, TimeUnit.MILLISECONDS);
            });

            sequence.add(proc -> {
                delay(2000);
                proc.setConfig(createConfig(originalProc.getConfig().getSchedulingStrategy(), originalProc.getConfig().getSchedulingPeriod()));
                return updateWithRetry(proc, 5, 300, TimeUnit.MILLISECONDS);
            });
        }

        // Disable or start the processor again depending on its original state
        if (state == PROCESS_STATE.DISABLED) {
            sequence.add((proc) -> {
                proc.setConfig(createConfig(originalProc.getConfig().getSchedulingStrategy(), originalProc.getConfig().getSchedulingPeriod()));
                proc.setState(PROCESS_STATE.DISABLED.name());
                return updateWithRetry(proc, 5, 300, TimeUnit.MILLISECONDS);
            });
        } else if (state == PROCESS_STATE.RUNNING) {
            sequence.add((proc) -> {
                proc.setConfig(createConfig(originalProc.getConfig().getSchedulingStrategy(), originalProc.getConfig().getSchedulingPeriod()));
                proc.setState(PROCESS_STATE.RUNNING.name());
                return updateWithRetry(proc, 5, 300, TimeUnit.MILLISECONDS);
            });
        }
        
        return sequence;
    }
    
    private ProcessorDTO createProcessor(ProcessorDTO current) {
        ProcessorDTO proc = new ProcessorDTO();
        proc.setId(current.getId());
        return proc;
    }
    
    private ProcessorConfigDTO createConfig(String schedStrategy, String schedPeriod) {
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setSchedulingStrategy(schedStrategy);
        config.setSchedulingPeriod(schedPeriod);
        return config;
    }
    
    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Do nothing
        }
    }
}
