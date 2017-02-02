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

    Date getPreviousFireTime();

    Date getStartTime();

    Date getEndTime();

    String getCronExpression();

    String getCronExpressionSummary();

    String getDescription();

    boolean isSimpleTrigger();

    void setSimpleTrigger(boolean isSimpleTrigger);

    boolean isScheduled();

    void setScheduled(boolean scheduled);

    TriggerInfo.TriggerState getState();

    TriggerIdentifier getTriggerIdentifier();

    JobIdentifier getJobIdentifier();

    void setTriggerIdentifier(TriggerIdentifier triggerIdentifier);

    void setJobIdentifier(JobIdentifier jobIdentifier);

    void setNextFireTime(Date nextFireTime);

    void setPreviousFireTime(Date previousFireTime);

    void setStartTime(Date startTime);

    void setEndTime(Date endTime);

    void setCronExpression(String cronExpression);

    void setCronExpressionSummary(String summary);

    void setDescription(String description);

    void setState(TriggerInfo.TriggerState state);

    void setTriggerClass(Class triggerClass);

    Class getTriggerClass();

    public static enum TriggerState {
        NONE,
        NORMAL,
        PAUSED,
        COMPLETE,
        ERROR,
        BLOCKED;
    }
}
