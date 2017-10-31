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

import com.thinkbiganalytics.cluster.ClusterMessage;
import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.cluster.ClusterServiceMessageReceiver;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Receive Quartz Schedule messages from other kylo nodes
 */
public class QuartzClusterMessageReceiver implements ClusterServiceMessageReceiver {

    @Inject
    JobScheduler scheduler;

    @Inject
    QuartzClusterMessageSender quartzClusterMessageSender;


    @Inject
    ClusterService clusterService;

    @PostConstruct
    private void init(){
    clusterService.subscribe(this, Arrays.stream(QuartzClusterMessage.QUARTZ_CLUSTER_MESSAGE_TYPE.values()).map(Enum::name).toArray(String[]::new));
    }



    private boolean accepts(ClusterMessage msg){
        boolean accept = false;
       try {
           QuartzClusterMessage.QUARTZ_CLUSTER_MESSAGE_TYPE.valueOf(msg.getType());
           accept = true;
       }catch (IllegalArgumentException e){

       }
       return accept;

    }



    @Override
    public void onMessageReceived(String from, ClusterMessage message) {

        if(accepts(message)){
            try {
                quartzClusterMessageSender.setShouldSend(false);
                QuartzClusterMessage.QUARTZ_CLUSTER_MESSAGE_TYPE type = QuartzClusterMessage.QUARTZ_CLUSTER_MESSAGE_TYPE.valueOf(message.getType());
                switch (type) {
                    case QTZ_SCHEDULER_PAUSED:
                        scheduler.pauseScheduler();
                        break;
                    case QTZ_SCHEDULER_RESUMED:
                        scheduler.startScheduler();
                        break;
                    case QTZ_JOB_PAUSED:
                        scheduler.pauseTriggersOnJob((JobIdentifier) message.getMessage());
                        break;
                    case QTZ_JOB_RESUMED:
                        scheduler.resumeTriggersOnJob((JobIdentifier) message.getMessage());
                        break;
                    default:
                        break;
                }
            }catch (JobSchedulerException e) {

            }
            finally {
                quartzClusterMessageSender.setShouldSend(true);
            }
        }

    }
}
