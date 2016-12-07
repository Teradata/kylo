package com.thinkbiganalytics.metadata.jpa.jobrepo;

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
