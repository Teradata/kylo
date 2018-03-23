package com.thinkbiganalytics.nifi.savepoint.model;

import java.io.Serializable;

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
public class SavepointReplayEvent implements Serializable{

    private Long jobExecutionId;
    private String flowfileId;
    public enum Action {
        RETRY, RELEASE
    }
    private Action action;


    public SavepointReplayEvent(){

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

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SavepointReplayEvent{");
        sb.append("flowfileId='").append(flowfileId).append('\'');
        sb.append(", action=").append(action);
        sb.append('}');
        return sb.toString();
    }
}
