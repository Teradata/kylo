package com.thinkbiganalytics.scheduler;

/*-
 * #%L
 * thinkbig-scheduler-api
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


import java.util.Date;

/**
 * Information about a scheduled items trigger indicating its schedule and when it should be fired next
 */
public interface TriggerInfo {

    Date getNextFireTime();

    void setNextFireTime(Date nextFireTime);

    Date getPreviousFireTime();

    void setPreviousFireTime(Date previousFireTime);

    Date getStartTime();

    void setStartTime(Date startTime);

    Date getEndTime();

    void setEndTime(Date endTime);

    String getCronExpression();

    void setCronExpression(String cronExpression);

    String getCronExpressionSummary();

    void setCronExpressionSummary(String summary);

    String getDescription();

    void setDescription(String description);

    boolean isSimpleTrigger();

    void setSimpleTrigger(boolean isSimpleTrigger);

    boolean isScheduled();

    void setScheduled(boolean scheduled);

    TriggerInfo.TriggerState getState();

    void setState(TriggerInfo.TriggerState state);

    TriggerIdentifier getTriggerIdentifier();

    void setTriggerIdentifier(TriggerIdentifier triggerIdentifier);

    JobIdentifier getJobIdentifier();

    void setJobIdentifier(JobIdentifier jobIdentifier);

    Class getTriggerClass();

    void setTriggerClass(Class triggerClass);

    public static enum TriggerState {
        NONE,
        NORMAL,
        PAUSED,
        COMPLETE,
        ERROR,
        BLOCKED;
    }
}
