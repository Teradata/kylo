package com.thinkbiganalytics.nifi.v2.core.savepoint;
/*-
 * #%L
 * thinkbig-nifi-core-service
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

import com.thinkbiganalytics.jms.SendJmsMessage;
import com.thinkbiganalytics.nifi.savepoint.api.SavepointQueues;
import com.thinkbiganalytics.nifi.savepoint.model.SavepointReplayEvent;
import com.thinkbiganalytics.nifi.savepoint.model.SavepointReplayResponseEvent;

import javax.inject.Inject;

public class SavepointReplayResponseJmsProducer {

    @Inject
    private SendJmsMessage sendJmsMessage;

    public void replayFailure(SavepointReplayEvent entry, String message) {
        SavepointReplayResponseEvent response = new SavepointReplayResponseEvent();
        response.setJobExecutionId(entry.getJobExecutionId());
        response.setFlowfileId(entry.getFlowfileId());
        response.setResponse(SavepointReplayResponseEvent.RESPONSE.FAILURE);
        response.setAction(entry.getAction());
        response.setMessage(message);
        sendJmsMessage.sendSerializedObjectToQueue(SavepointQueues.REPLAY_SAVEPOINT_RESPONE_QUEUE, response);
    }

    public void replaySuccess(SavepointReplayEvent entry, String message) {
        SavepointReplayResponseEvent response = new SavepointReplayResponseEvent();
        response.setJobExecutionId(entry.getJobExecutionId());
        response.setFlowfileId(entry.getFlowfileId());
        response.setResponse(SavepointReplayResponseEvent.RESPONSE.SUCCESS);
        response.setAction(entry.getAction());
        response.setMessage(message);
        sendJmsMessage.sendSerializedObjectToQueue(SavepointQueues.REPLAY_SAVEPOINT_RESPONE_QUEUE, response);
    }
}
