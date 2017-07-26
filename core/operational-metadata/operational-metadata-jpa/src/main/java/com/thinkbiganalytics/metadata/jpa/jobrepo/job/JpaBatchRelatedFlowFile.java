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

import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchRelatedFlowFile;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by sr186054 on 6/17/17.
 */
@Entity
@Table(name = "BATCH_RELATED_FLOW_FILES")
public class JpaBatchRelatedFlowFile implements BatchRelatedFlowFile {

    @Id
    @Column(name = "FLOW_FILE_ID")
    private String flowFileId;


    @Column(name = "BATCH_JOB_FLOW_FILE_ID")
    private String batchJobFlowFileId;

    @Column(name = "BATCH_JOB_EXECUTION_ID")
    private Long batchJobExecutionId;

    public JpaBatchRelatedFlowFile() {
    }

    public JpaBatchRelatedFlowFile(String flowFileId, String batchJobFlowFileId, Long batchJobExecutionId) {
        this.flowFileId = flowFileId;
        this.batchJobFlowFileId = batchJobFlowFileId;
        this.batchJobExecutionId = batchJobExecutionId;
    }

    @Override
    public String getFlowFileId() {
        return flowFileId;
    }

    public void setFlowFileId(String flowFileId) {
        this.flowFileId = flowFileId;
    }

    @Override
    public String getBatchJobFlowFileId() {
        return batchJobFlowFileId;
    }

    public void setBatchJobFlowFileId(String batchJobFlowFileId) {
        this.batchJobFlowFileId = batchJobFlowFileId;
    }

    public Long getBatchJobExecutionId() {
        return batchJobExecutionId;
    }

    public void setBatchJobExecutionId(Long batchJobExecutionId) {
        this.batchJobExecutionId = batchJobExecutionId;
    }
}
