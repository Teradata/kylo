package com.thinkbiganalytics.jobrepo.service;

import com.thinkbiganalytics.jobrepo.query.model.ExecutionStatus;
import com.thinkbiganalytics.jobrepo.query.model.JobParameterType;
import com.thinkbiganalytics.jobrepo.repository.dao.JobDao;
import com.thinkbiganalytics.jobrepo.repository.dao.PipelineDao;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 4/14/16.
 */
@Named
public abstract class AbstractJobService implements JobService {


    @Autowired
    private PipelineDao pipelineDao;


    @Autowired
    private JobDao jobDao;


    public Map<ExecutionStatus, Long> getJobStatusCount() {
        return jobDao.getCountOfJobsByStatus();
    }

    public Map<ExecutionStatus, Long> getLatestJobStatusCount() {
        return jobDao.getCountOfLatestJobsByStatus();
    }

    public List<JobParameterType> getJobParametersForJob(String jobName) {
        return jobDao.getJobParametersForJob(jobName);
    }

    public List<JobParameterType> getJobParametersForJob(Long executionId) {
        return jobDao.getJobParametersForJob(executionId);
    }


}
