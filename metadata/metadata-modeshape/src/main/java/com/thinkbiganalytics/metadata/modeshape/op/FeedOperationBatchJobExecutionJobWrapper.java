package com.thinkbiganalytics.metadata.modeshape.op;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Wrapper object to overlay the BatchJobExecution object to allow interaction with the JobExecution data against other metadata operations like Precondition Assessments
 */
public class FeedOperationBatchJobExecutionJobWrapper implements FeedOperation {

    private final OpId id;
    private final BatchJobExecution executed;

    FeedOperationBatchJobExecutionJobWrapper(BatchJobExecution jobExecution) {
        this.id = new OpId(jobExecution.getJobExecutionId());
        this.executed = jobExecution;
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.FeedOperation#getId()
     */
    @Override
    public ID getId() {
        return this.id;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.FeedOperation#getStartTime()
     */
    @Override
    public DateTime getStartTime() {
        return this.executed.getStartTime();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.FeedOperation#getStopTime()
     */
    @Override
    public DateTime getStopTime() {
        return this.executed.getEndTime();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.FeedOperation#getState()
     */
    @Override
    public State getState() {
        return JobRepoFeedOperationsProvider.asOperationState(this.executed.getStatus());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.FeedOperation#getStatus()
     */
    @Override
    public String getStatus() {
        return this.executed.getStatus().name();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.FeedOperation#getResults()
     */
    @Override
    public Map<String, Object> getResults() {
        Map<String, Object> results = new HashMap<>();
        results.putAll(this.executed.getJobExecutionContextAsMap());
        return results;
    }


    protected static class OpId implements FeedOperation.ID {

        private final String idValue;

        public OpId(Serializable value) {
            this.idValue = value.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (getClass().isAssignableFrom(obj.getClass())) {
                OpId that = (OpId) obj;
                return Objects.equals(this.idValue, that.idValue);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.idValue);
        }

        @Override
        public String toString() {
            return this.idValue;
        }
    }

}
