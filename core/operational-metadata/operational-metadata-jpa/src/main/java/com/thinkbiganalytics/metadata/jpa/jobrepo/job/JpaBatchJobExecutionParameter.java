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

import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionParameter;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;


/**
 * An entity storing the parameters that are initially captured when running a {@link BatchJobExecution}
 */
@Entity
@Table(name = "BATCH_JOB_EXECUTION_PARAMS")
public class JpaBatchJobExecutionParameter implements Serializable, BatchJobExecutionParameter {


    @EmbeddedId
    private BatchJobExecutionParametersPK jobExecutionParametersPK;
    @MapsId("jobExecutionId")
    @ManyToOne(targetEntity = JpaBatchJobExecution.class, optional = false)
    @JoinColumn(name = "JOB_EXECUTION_ID", referencedColumnName = "JOB_EXECUTION_ID")
    private BatchJobExecution jobExecution;
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE_CD", length = 10, nullable = false)
    private ExecutionConstants.ParamType typeCode = ExecutionConstants.ParamType.STRING;
    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "250")})
    @Column(name = "STRING_VAL")
    private String stringVal;
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "DATE_VAL")
    private DateTime dateVal;
    @Column(name = "LONG_VAL")
    private Long longVal;
    @Column(name = "DOUBLE_VAL")
    private Double doubleVal;

    public JpaBatchJobExecutionParameter() {

    }

    @Override
    public String getKeyName() {
        return getJobExecutionParametersPK().getKeyName();
    }

    public BatchJobExecutionParametersPK getJobExecutionParametersPK() {
        return jobExecutionParametersPK;
    }

    public void setJobExecutionParametersPK(BatchJobExecutionParametersPK jobExecutionParametersPK) {
        this.jobExecutionParametersPK = jobExecutionParametersPK;
    }

    @Override
    public BatchJobExecution getJobExecution() {
        return jobExecution;
    }

    public void setJobExecution(BatchJobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }

    @Override
    public ExecutionConstants.ParamType getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(ExecutionConstants.ParamType typeCode) {
        this.typeCode = typeCode;
    }

    @Override
    public String getStringVal() {
        return stringVal;
    }

    public void setStringVal(String stringVal) {
        this.stringVal = stringVal;
    }

    @Override
    public DateTime getDateVal() {
        return dateVal;
    }

    public void setDateVal(DateTime dateVal) {
        this.dateVal = dateVal;
    }

    @Override
    public Long getLongVal() {
        return longVal;
    }

    public void setLongVal(Long longVal) {
        this.longVal = longVal;
    }

    @Override
    public Double getDoubleVal() {
        return doubleVal;
    }

    public void setDoubleVal(Double doubleVal) {
        this.doubleVal = doubleVal;
    }

    @Embeddable
    public static class BatchJobExecutionParametersPK implements Serializable {

        @Column(name = "JOB_EXECUTION_ID")
        private Long jobExecutionId;

        @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "250")})
        @Column(name = "KEY_NAME")
        private String keyName;

        public BatchJobExecutionParametersPK() {

        }

        public BatchJobExecutionParametersPK(Long jobExecutionId, String keyName) {
            this.jobExecutionId = jobExecutionId;
            this.keyName = keyName;
        }

        public Long getJobExecutionId() {
            return jobExecutionId;
        }

        public void setJobExecutionId(Long jobExecutionId) {
            this.jobExecutionId = jobExecutionId;
        }

        public String getKeyName() {
            return keyName;
        }

        public void setKeyName(String keyName) {
            this.keyName = keyName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BatchJobExecutionParametersPK that = (BatchJobExecutionParametersPK) o;

            if (!jobExecutionId.equals(that.jobExecutionId)) {
                return false;
            }
            return keyName.equals(that.keyName);

        }

        @Override
        public int hashCode() {
            int result = jobExecutionId.hashCode();
            result = 31 * result + keyName.hashCode();
            return result;
        }
    }


}
