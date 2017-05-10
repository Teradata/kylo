package com.thinkbiganalytics.scheduler;

/*-
 * #%L
 * thinkbig-scheduler-quartz
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

import com.thinkbiganalytics.cluster.ClusterService;

import javax.inject.Inject;

/**
 * Send Quartz scheduler messages to other kylo nodes
 */
public class QuartzClusterMessageSender {

    private boolean shouldSend = true;

    @Inject
    private ClusterService clusterService;

    public void notifySchedulerPaused(){
        if(clusterService.isClustered() && shouldSend) {
            String message = "Scheduler Paused";
            clusterService.sendMessageToOthers(QuartzClusterMessage.QUARTZ_CLUSTER_MESSAGE_TYPE.QTZ_SCHEDULER_PAUSED.name(),message);
        }
    }

    public void notifySchedulerResumed(){
        if(clusterService.isClustered() && shouldSend) {
            String message = "Scheduler Resumed";
            clusterService.sendMessageToOthers(QuartzClusterMessage.QUARTZ_CLUSTER_MESSAGE_TYPE.QTZ_SCHEDULER_RESUMED.name(),message);
        }
    }

    public void notifyJobPaused(JobIdentifier jobIdentifier){
        if(clusterService.isClustered() && shouldSend)  {
            clusterService.sendMessageToOthers(QuartzClusterMessage.QUARTZ_CLUSTER_MESSAGE_TYPE.QTZ_JOB_PAUSED.name(),jobIdentifier);
        }
    }

    public void notifyJobResumed(JobIdentifier jobIdentifier){
        if(clusterService.isClustered() && shouldSend) {
            clusterService.sendMessageToOthers(QuartzClusterMessage.QUARTZ_CLUSTER_MESSAGE_TYPE.QTZ_JOB_RESUMED.name(),jobIdentifier);
        }
    }

    public void setShouldSend(boolean shouldSend) {
        this.shouldSend = shouldSend;
    }
}
