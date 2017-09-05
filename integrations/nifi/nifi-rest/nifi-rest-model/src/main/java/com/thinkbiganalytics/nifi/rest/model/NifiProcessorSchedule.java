package com.thinkbiganalytics.nifi.rest.model;

/*-
 * #%L
 * thinkbig-nifi-rest-model
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


import com.thinkbiganalytics.metadata.MetadataField;

/**
 * Hold Schedule information for the feed
 */
public class NifiProcessorSchedule {


    private String processorId;

    @MetadataField
    private String schedulingPeriod;
    @MetadataField
    private String schedulingStrategy;
    @MetadataField
    private Integer concurrentTasks;
    @MetadataField
    private String executionNode;


    public NifiProcessorSchedule() {

    }

    public NifiProcessorSchedule(NifiProcessorSchedule other) {
        this.processorId = other.processorId;
        this.schedulingPeriod = other.schedulingPeriod;
        this.schedulingStrategy = other.schedulingStrategy;
        this.concurrentTasks = other.concurrentTasks;
        this.executionNode = other.executionNode;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    /**
     * Return the NiFi schedule period.
     * If the {@link #schedulingStrategy} is set to "TIMER_DRIVEN" this will either be a timer string (i.e. 1 hr, 5 sec)
     * If the {@link #schedulingStrategy} is set to "CRON_DRIVEN" this will be the cron expression
     *
     * @return the schedule period or cron expression
     */
    public String getSchedulingPeriod() {
        return schedulingPeriod;
    }

    /**
     * set the schedule period (either timer string or cron expression)
     *
     * @param schedulingPeriod the schedule period (either timer string or cron expression)
     */
    public void setSchedulingPeriod(String schedulingPeriod) {
        this.schedulingPeriod = schedulingPeriod;
    }

    /**
     * Return the strategy, "TIMER_DRIVEN","CRON_DRIVEN","TRIGGER_DRIVEN".  This strategy is used with the {@link #schedulingPeriod}
     *
     * @return the strategy, "TIMER_DRIVEN","CRON_DRIVEN","TRIGGER_DRIVEN"
     */
    public String getSchedulingStrategy() {
        return schedulingStrategy;
    }

    public void setSchedulingStrategy(String schedulingStrategy) {
        this.schedulingStrategy = schedulingStrategy;
    }

    /**
     * The number of concurrent tasks allowed for the processor
     *
     * @return the number of concurrent tasks allowed for the processor
     */
    public Integer getConcurrentTasks() {
        return concurrentTasks;
    }

    public void setConcurrentTasks(Integer concurrentTasks) {
        this.concurrentTasks = concurrentTasks;
    }

    /**
     * The node(s) that this processor will be scheduled to run on.
     *
     * @return one of: ALL, PRIMARY
     */
    public String getExecutionNode() {
        return executionNode;
    }

    public void setExecutionNode(String executionNode) {
        this.executionNode = executionNode;
    }


}
