package com.thinkbiganalytics.jobrepo.service;

import com.thinkbiganalytics.jobrepo.repository.dao.ExecutionContextValuesDao;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by sr186054 on 8/27/15.
 */
@Named
public class ExecutionContextValuesServiceImpl implements ExecutionContextValuesService {

    @Inject
    private ExecutionContextValuesDao executionContextValuesDao;


    public void saveStepExecutionContextValues(StepExecution stepExecution) {
        executionContextValuesDao.saveStepExecutionContextValues(stepExecution);
    }

    /**
     * Save specific parameters matching the keys passed in via the List
     *
     * @param stepExecution
     * @param stepExecutionKeys
     */
    public void saveStepExecutionContextValues(StepExecution stepExecution, List<String> stepExecutionKeys) {

        executionContextValuesDao.saveStepExecutionContextValues(stepExecution, stepExecutionKeys);

    }

    public void saveJobExecutionContextValues(JobExecution jobExecution) {
        executionContextValuesDao.saveJobExecutionContextValues(jobExecution);
    }


    public void saveJobExecutionContextValues(JobExecution jobExecution, List<String> jobExecutionKeys) {
        executionContextValuesDao.saveJobExecutionContextValues(jobExecution, jobExecutionKeys);

    }
}
