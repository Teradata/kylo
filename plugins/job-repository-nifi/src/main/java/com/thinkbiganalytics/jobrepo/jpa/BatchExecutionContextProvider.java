package com.thinkbiganalytics.jobrepo.jpa;

import com.thinkbiganalytics.jobrepo.jpa.model.BatchJobExecutionContext;
import com.thinkbiganalytics.jobrepo.jpa.model.BatchStepExecutionContext;

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

    @Autowired
    ExecutionContextSerializationHelper exectionContextSerializationHelper;

    private BatchStepExecutionContextRepository batchStepExecutionContextRepository;
    private BatchJobExecutionContextRepository batchJobExecutionContextRepository;

    @Autowired
    BatchExecutionContextProvider(BatchStepExecutionContextRepository batchStepExecutionContextRepository, BatchJobExecutionContextRepository batchJobExecutionContextRepository) {
        this.batchStepExecutionContextRepository = batchStepExecutionContextRepository;
        this.batchJobExecutionContextRepository = batchJobExecutionContextRepository;
    }


    public void saveStepExecutionContext(Long stepExecutionId, Map<String, String> attrs) {
        if (attrs != null && !attrs.isEmpty()) {

            BatchStepExecutionContext stepExecutionContext = batchStepExecutionContextRepository.findOne(stepExecutionId);
            if (stepExecutionContext == null) {
                stepExecutionContext = new BatchStepExecutionContext();
                stepExecutionContext.setStepExecutionId(stepExecutionId);
            }
            String serializedContext = exectionContextSerializationHelper.serializeStringMapContext(attrs);
            stepExecutionContext.setShortContext(serializedContext);
            stepExecutionContext.setSerializedContext(serializedContext);
            batchStepExecutionContextRepository.save(stepExecutionContext);
        }

    }

    public void saveJobExecutionContext(Long jobExecutionId, Map<String, String> attrs) {

        if (attrs != null && !attrs.isEmpty()) {
            BatchJobExecutionContext executionContext = batchJobExecutionContextRepository.findOne(jobExecutionId);
            if (executionContext == null) {
                executionContext = new BatchJobExecutionContext();
                executionContext.setJobExecutionId(jobExecutionId);
            }
            String serializedContext = exectionContextSerializationHelper.serializeStringMapContext(attrs);
            executionContext.setShortContext(serializedContext);
            executionContext.setSerializedContext(serializedContext);
            batchJobExecutionContextRepository.save(executionContext);
        }

    }


}
