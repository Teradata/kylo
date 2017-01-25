package com.thinkbiganalytics.metadata.jpa.jobrepo;

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

import com.thinkbiganalytics.metadata.jpa.jobrepo.job.BatchJobExecutionContextRepository;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.JpaBatchJobExecutionContext;
import com.thinkbiganalytics.metadata.jpa.jobrepo.step.BatchStepExecutionContextRepository;
import com.thinkbiganalytics.metadata.jpa.jobrepo.step.JpaBatchStepExecutionContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by sr186054 on 9/1/16.
 */
@Service
public class BatchExecutionContextProvider {

    public BatchExecutionContextProvider() {

    }

    private BatchStepExecutionContextRepository batchStepExecutionContextRepository;
    private BatchJobExecutionContextRepository batchJobExecutionContextRepository;

    @Autowired
    BatchExecutionContextProvider(BatchStepExecutionContextRepository batchStepExecutionContextRepository, BatchJobExecutionContextRepository batchJobExecutionContextRepository) {
        this.batchStepExecutionContextRepository = batchStepExecutionContextRepository;
        this.batchJobExecutionContextRepository = batchJobExecutionContextRepository;
    }


    public void saveStepExecutionContext(Long stepExecutionId, Map<String, String> attrs) {
        if (attrs != null && !attrs.isEmpty()) {

            JpaBatchStepExecutionContext stepExecutionContext = batchStepExecutionContextRepository.findOne(stepExecutionId);
            if (stepExecutionContext == null) {
                stepExecutionContext = new JpaBatchStepExecutionContext();
                stepExecutionContext.setStepExecutionId(stepExecutionId);
            }
            batchStepExecutionContextRepository.save(stepExecutionContext);
        }

    }

    public void saveJobExecutionContext(Long jobExecutionId, Map<String, String> attrs) {

        if (attrs != null && !attrs.isEmpty()) {
            JpaBatchJobExecutionContext executionContext = batchJobExecutionContextRepository.findOne(jobExecutionId);
            if (executionContext == null) {
                executionContext = new JpaBatchJobExecutionContext();
                executionContext.setJobExecutionId(jobExecutionId);
            }
            batchJobExecutionContextRepository.save(executionContext);
        }

    }


}
