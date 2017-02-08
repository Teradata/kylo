package com.thinkbiganalytics.metadata.jpa.jobrepo.job;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionContextValue;
import com.thinkbiganalytics.metadata.jpa.jobrepo.AbstractBatchExecutionContextValue;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity to store execution context data for a job.
 * As a job executes it will capture data along the way.  This data is saved in this entity
 */
@Entity
@Table(name = "BATCH_JOB_EXECUTION_CTX_VALS")
public class JpaBatchJobExecutionContextValue extends AbstractBatchExecutionContextValue implements BatchJobExecutionContextValue {


    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", unique = true)
    private String id;


    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "250")})
    @Column(name = "KEY_NAME")
    private String keyName;

    @Column(name = "JOB_EXECUTION_ID")
    private Long jobExecutionId;

    public JpaBatchJobExecutionContextValue() {

    }

    public JpaBatchJobExecutionContextValue(BatchJobExecution jobExecution, String keyName) {
        setJobExecutionId(jobExecution.getJobExecutionId());
        this.keyName = keyName;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    @Override
    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JpaBatchJobExecutionContextValue that = (JpaBatchJobExecutionContextValue) o;

        if (keyName != null ? !keyName.equals(that.keyName) : that.keyName != null) {
            return false;
        }
        return !(jobExecutionId != null ? !jobExecutionId.equals(that.jobExecutionId) : that.jobExecutionId != null);

    }

    @Override
    public int hashCode() {
        int result = keyName != null ? keyName.hashCode() : 0;
        result = 31 * result + (jobExecutionId != null ? jobExecutionId.hashCode() : 0);
        return result;
    }
}
