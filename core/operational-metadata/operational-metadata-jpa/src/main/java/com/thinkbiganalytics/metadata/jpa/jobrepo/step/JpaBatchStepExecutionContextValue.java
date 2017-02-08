package com.thinkbiganalytics.metadata.jpa.jobrepo.step;

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

import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecutionContextValue;
import com.thinkbiganalytics.metadata.jpa.jobrepo.AbstractBatchExecutionContextValue;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 */
@Entity
@Table(name = "BATCH_STEP_EXECUTION_CTX_VALS")
public class JpaBatchStepExecutionContextValue extends AbstractBatchExecutionContextValue implements BatchStepExecutionContextValue {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", unique = true)
    private String id;

    @ManyToOne(targetEntity = JpaBatchStepExecution.class)
    @JoinColumn(name = "STEP_EXECUTION_ID", referencedColumnName = "STEP_EXECUTION_ID")
    private BatchStepExecution stepExecution;

    @Column(name = "JOB_EXECUTION_ID")
    private Long jobExecutionId;


    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "250")})
    @Column(name = "KEY_NAME")
    private String keyName;

    public JpaBatchStepExecutionContextValue() {

    }

    public JpaBatchStepExecutionContextValue(BatchStepExecution stepExecution, String keyName) {
        setStepExecution(stepExecution);
        this.keyName = keyName;
        this.jobExecutionId = stepExecution.getJobExecution().getJobExecutionId();
    }

    @Override
    public BatchStepExecution getStepExecution() {
        return stepExecution;
    }

    public void setStepExecution(BatchStepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }


    @Override
    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
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

        JpaBatchStepExecutionContextValue that = (JpaBatchStepExecutionContextValue) o;

        if (stepExecution != null ? !stepExecution.equals(that.stepExecution) : that.stepExecution != null) {
            return false;
        }
        return !(keyName != null ? !keyName.equals(that.keyName) : that.keyName != null);

    }

    @Override
    public int hashCode() {
        int result = stepExecution != null ? stepExecution.hashCode() : 0;
        result = 31 * result + (keyName != null ? keyName.hashCode() : 0);
        return result;
    }
}
