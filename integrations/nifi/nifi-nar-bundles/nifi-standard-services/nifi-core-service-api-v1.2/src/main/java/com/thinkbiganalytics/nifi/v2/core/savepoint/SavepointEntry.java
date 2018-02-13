package com.thinkbiganalytics.nifi.v2.core.savepoint;

/*-
 * #%L
 * kylo-nifi-core-processors
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Stores state of a savepoint
 */
public class SavepointEntry {

    public enum SavePointState {
        WAIT, RETRY, RELEASE_SUCCESS, RELEASE_FAILURE;

        public boolean isRelease() {
            return this == RELEASE_FAILURE || this == RELEASE_SUCCESS;
        }
    }

    @JsonProperty
    private Map<String, Processor> processors = new HashMap<>();

    public SavepointEntry() {
        super();
    }

    public Processor remove(String processorId) {
        return processors.remove(processorId);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return processors.isEmpty();
    }

    public void register(String processorId, String flowFileId) {
        Processor processor = processors.getOrDefault(processorId, new Processor(processorId));
        processor.setProcessorId(processorId);
        if (processor.isEmpty()) {
            releaseAll(true);
            processor.setFlowFileId(flowFileId);
            processors.put(processorId, processor);
        } else {
            throw new InvalidSavePointId();
        }
    }

    public void releaseAll(boolean success) {
        if (!processors.isEmpty()) {
            processors.forEach((pid, p) -> {
                if (success) {
                    p.setState(SavePointState.RELEASE_SUCCESS);
                } else {
                    p.setState(SavePointState.RELEASE_FAILURE);
                }
            });
        }
    }

    public void retry() {
        boolean success = false;
        if (!processors.isEmpty()) {
            processors.forEach((pid, p) -> {
                if (p.state == SavePointState.WAIT) {
                    retry(pid);
                    return;
                }
            });
            success = true;
        }
        if (!success) {
            throw new RuntimeException("Failed to retry");
        }
    }

    public SavePointState getState(String processorId) {
        Processor processor = processors.get(processorId);
        return (processor == null ? null : processor.state);
    }

    public void release(String processorId, boolean success) {
        Processor processor = processors.get(processorId);
        if (processor == null) {
            throw new RuntimeException("Unable to release savepoint for processor [" + processorId + "]. No flowfiles registered.");
        }
        if (success) {
            processor.setState(SavePointState.RELEASE_SUCCESS);
        } else {
            processor.setState(SavePointState.RELEASE_FAILURE);
        }
    }

    public void retry(String processorId) {
        Processor processor = processors.get(processorId);
        if (processor == null) {
            throw new RuntimeException("Unable to retry savepoint for processor [" + processorId + "]. No flowfiles registered.");
        }
        if (processor.state.isRelease()) {
            throw new RuntimeException("Unable to retry savepoint for processor [" + processorId + "]. Already released.");
        }
        processor.setState(SavePointState.RETRY);
    }

    public void waitState(String processorId) {
        Processor processor = processors.get(processorId);
        if (processor == null) {
            throw new RuntimeException("Unable to retry savepoint for processor [" + processorId + "]. No flowfiles registered.");
        }
        if (processor.state.isRelease()) {
            throw new RuntimeException("Already released.");
        }
        processor.setState(SavePointState.WAIT);
    }

    @JsonIgnore
    public Collection<Processor> getProcessorList() {
        return processors.values();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Processor {

        @JsonProperty
        private String processorId;

        @JsonProperty
        private SavePointState state = SavePointState.WAIT;

        @JsonProperty
        private String flowFileId;

        public Processor() {
            super();
        }

        public Processor(String processorId) {
            this.processorId = processorId;
        }

        @JsonIgnore
        public boolean isEmpty() {
            return flowFileId == null;
        }

        public void setFlowFileId(String flowFileId) {
            this.flowFileId = flowFileId;
        }

        public void setState(SavePointState newState) {
            this.state = newState;
        }

        public String getFlowFileId() {
            return flowFileId;
        }

        public String getProcessorId() {
            return processorId;
        }

        public void setProcessorId(String processorId) {
            this.processorId = processorId;
        }
    }

}


