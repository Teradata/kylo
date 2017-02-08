package com.thinkbiganalytics.scheduler.support;

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

import com.thinkbiganalytics.scheduler.TriggerInfo;

import java.util.Collection;

/**
 * Check for status information for triggers
 */
public class ScheduledJobState {


    /**
     * Check to see if a list of triggers have at least 1 triggerInfo that is running
     *
     * @param triggerInfos a collection of trigger info
     * @return {@code true} if the list of triggerInfos have at least 1 that is running, {@code false} if none of the triggerInfos are running
     */
    public static boolean isRunning(Collection<TriggerInfo> triggerInfos) {
        boolean running = false;
        if (triggerInfos != null) {
            for (TriggerInfo triggerInfo : triggerInfos) {
                running = triggerInfo.getState().equals(TriggerInfo.TriggerState.BLOCKED) || triggerInfo.isSimpleTrigger();
                if (running) {
                    break;
                }
            }
        }
        return running;
    }

    /**
     * Check to see if a list of triggers have at least 1 triggerInfo that is paused
     *
     * @param triggerInfos a collection of trigger info
     * @return {@code true} if the list of triggerInfos have at least 1 that is paused, {@code false} if none of the triggerInfos are paused
     */
    public static boolean isPaused(Collection<TriggerInfo> triggerInfos) {
        boolean paused = false;
        if (triggerInfos != null) {
            for (TriggerInfo triggerInfo : triggerInfos) {
                paused = triggerInfo.getState().equals(TriggerInfo.TriggerState.PAUSED);
                if (paused) {
                    break;
                }
            }
        }
        return paused;
    }

    /**
     * Check to see if a list of triggers have at least 1 triggerInfo that is scheduled
     *
     * @param triggerInfos a collection of trigger info
     * @return {@code true} if the list of triggerInfos have at least 1 that is scheduled, {@code false} if none of the triggerInfos are scheduled
     */
    public static boolean isScheduled(Collection<TriggerInfo> triggerInfos) {
        boolean scheduled = false;
        if (triggerInfos != null) {
            for (TriggerInfo triggerInfo : triggerInfos) {
                if (triggerInfo.isScheduled()) {
                    scheduled = true;
                    break;
                }
            }
        }
        return scheduled;
    }
}
