package com.thinkbiganalytics.nifi.savepoint.model;
/*-
 * #%L
 * kylo-nifi-savepoint-model
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
import java.io.Serializable;

public class SavepointReplayResponseEvent implements Serializable {

    private Long jobExecutionId;
    private String flowfileId;
    private RESPONSE response;
    private SavepointReplayEvent.Action action;
    private String message;
    public static enum RESPONSE {
        SUCCESS,FAILURE
    }

    public SavepointReplayEvent.Action getAction() {
        return action;
    }

    public void setAction(SavepointReplayEvent.Action action) {
        this.action = action;
    }

    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    public String getFlowfileId() {
        return flowfileId;
    }

    public void setFlowfileId(String flowfileId) {
        this.flowfileId = flowfileId;
    }

    public RESPONSE getResponse() {
        return response;
    }

    public void setResponse(RESPONSE response) {
        this.response = response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
