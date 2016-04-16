package com.thinkbiganalytics.jobrepo.repository.dao;

import com.thinkbiganalytics.jobrepo.query.job.*;
import com.thinkbiganalytics.jobrepo.query.model.*;
import com.thinkbiganalytics.jobrepo.query.support.*;
import com.thinkbiganalytics.jobrepo.repository.JobRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import javax.inject.Named;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 8/13/15.
 */
@Named
public class JobDao extends BaseQueryDao {
    private static Logger LOG = LoggerFactory.getLogger(JobDao.class);

    public List<Object> selectDistinctColumnValues(List<ColumnFilter> conditions, String column) {
        JobQuery jobQuery = getJobQuery(conditions);
        jobQuery.setColumnFilterList(conditions);
        return selectDistinctColumnValues(jobQuery, column);
    }

    public Long selectCount(List<ColumnFilter> conditions) {
        JobQuery jobQuery = getJobQuery(conditions);
        jobQuery.setColumnFilterList(conditions);
        return selectCount(jobQuery);
    }

    public JobQuery getJobQuery(List<ColumnFilter> conditions) {

        if (ColumnFilterUtil.hasFilter(conditions, JobQueryConstants.USE_ACTIVE_JOBS_QUERY)) {
            return new ActiveJobQuery(getDatabaseType());
        } else {
            return new JobQuery(getDatabaseType());
        }
    }

    public List<ExecutedJob> findAllExecutedJobs(List<ColumnFilter> conditions, List<OrderBy> order, final Integer start, final Integer limit) {
        JobQuery jobQuery = getJobQuery(conditions);

        jobQuery.setColumnFilterList(conditions);
        jobQuery.setOrderByList(order);
        return findAllExecutedJobs(jobQuery, start, limit);


    }

    public List<ExecutedJob> findLatestExecutedJobs(List<ColumnFilter> conditions, List<OrderBy> order, final Integer start, final Integer limit) {
        LatestJobExecutionsQuery jobQuery = new LatestJobExecutionsQuery(getDatabaseType());
        jobQuery.setColumnFilterList(conditions);
        jobQuery.setOrderByList(order);
        return findLatestExecutedJobs(jobQuery, start, limit);
    }

    public Long selectLatestExecutedJobsCount(List<ColumnFilter> conditions) {
        LatestJobExecutionsQuery jobQuery = new LatestJobExecutionsQuery(getDatabaseType());
        jobQuery.setColumnFilterList(conditions);
        return selectCount(jobQuery);
    }

    public List<ExecutedJob> findAllExecutedJobs(JobQuery jobQuery, final Integer start, final Integer limit) {
        List<ExecutedJob> jobs = new ArrayList<ExecutedJob>();

        try {
            List<TbaJobExecution> jobExecutions = findList(jobQuery, start, limit);
            for (JobExecution je : jobExecutions) {
                ExecutedJob executedJob = JobRepositoryImpl.convertToExecutedJob(je.getJobInstance(), je);
                jobs.add(executedJob);
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return jobs;
    }

    public List<ExecutedJob> findLatestExecutedJobs(LatestJobExecutionsQuery jobQuery, final Integer start, final Integer limit) {
        List<ExecutedJob> jobs = new ArrayList<ExecutedJob>();

        try {
            List<TbaJobExecution> jobExecutions = findList(jobQuery, start, limit);
            for (JobExecution je : jobExecutions) {
                ExecutedJob executedJob = JobRepositoryImpl.convertToExecutedJob(je.getJobInstance(), je);
                jobs.add(executedJob);
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return jobs;
    }


    public List<ExecutedJob> findChildJobs(String parentJobExecutionId, boolean includeParent, final Integer start, final Integer limit) {
        List<ExecutedJob> jobs = new ArrayList<ExecutedJob>();
        ChildJobsQuery childJobsQuery = new ChildJobsQuery(getDatabaseType(), parentJobExecutionId, includeParent);

        try {
            if (includeParent) {
                List<ColumnFilter> conditions = new ArrayList<ColumnFilter>();
                conditions.add(new QueryColumnFilterSqlString("JOB_INSTANCE_ID", parentJobExecutionId));
                JobQuery jobQuery = new JobQuery(getDatabaseType());
                jobQuery.setColumnFilterList(conditions);
                List<ExecutedJob> parentJobs = findAllExecutedJobs(jobQuery, 0, null);
                if (parentJobs != null) {
                    jobs.addAll(parentJobs);
                }
            }
            List<JobExecution> jobExecutions = findList(childJobsQuery, start, limit);
            if (jobExecutions != null) {
                for (JobExecution je : jobExecutions) {
                    ExecutedJob executedJob = JobRepositoryImpl.convertToExecutedJob(je.getJobInstance(), je);
                    jobs.add(executedJob);
                }
            }


        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return jobs;
    }


    public Map<ExecutionStatus, Long> getCountOfJobsByStatus() {
        JobStatusCountQuery query = new JobStatusCountQuery(getDatabaseType());
        List<JobStatusCount> queryResult = findList(query, 0, null);
        return DaoUtil.convertJobExecutionStatusCountResult(queryResult);
    }


    public Map<ExecutionStatus, Long> getCountOfLatestJobsByStatus() {
        LatestJobStatusCountQuery query = new LatestJobStatusCountQuery(getDatabaseType());
        List<JobStatusCount> queryResult = findList(query, 0, null);
        return DaoUtil.convertJobExecutionStatusCountResult(queryResult);
    }

    private Long getCountForStatus(JobStatusCountQuery query, String status) {
        Long count = 0L;
        List<ColumnFilter> filters = new ArrayList<>();
        filters.add(new QueryColumnFilterSqlString("STATUS", status));
        query.setColumnFilterList(filters);
        List<JobStatusCount> queryResult = findList(query, 0, null);
        if (queryResult != null && !queryResult.isEmpty()) {
            count = queryResult.get(0).getCount();
        }
        return count;
    }


    public Long getCountOfJobsForStatus(String status) {
        JobStatusCountQuery query = new JobStatusCountQuery(getDatabaseType());
        return getCountForStatus(query, status);
    }

    public Long getCountOfLatestJobsForStatus(String status) {
        LatestJobStatusCountQuery query = new LatestJobStatusCountQuery(getDatabaseType());
        return getCountForStatus(query, status);
    }


    public List<JobParameterType> getJobParametersForJob(String jobName) {
        List<JobParameterType> list = new ArrayList<JobParameterType>();
        try {
            String sql = "SELECT KEY_NAME,TYPE_CD,STRING_VAL FROM BATCH_JOB_EXECUTION_PARAMS p" +
                    "                inner join (SELECT MAX(e.JOB_EXECUTION_ID) as JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION e inner join BATCH_JOB_INSTANCE ji on ji.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID" +
                    "                and ji.JOB_NAME = ?) max_job_execution on max_job_execution.JOB_EXECUTION_ID = p.JOB_EXECUTION_ID " +
                    " ORDER by KEY_NAME asc";
            Object[] args = new Object[]{jobName};

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args);
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    list.add(new JobParameterTypeImpl((String) row.get("KEY_NAME"), (String) row.get("STRING_VAL"), (String) row.get("TYPE_CD")));
                }
            }
        } catch (Exception e) {

        }
        return list;
    }


    public List<JobParameterType> getJobParametersForJob(Long executionId) {
        List<JobParameterType> list = new ArrayList<JobParameterType>();
        try {
            String sql = "SELECT KEY_NAME,TYPE_CD,STRING_VAL FROM BATCH_JOB_EXECUTION_PARAMS p" +
                    "              where p.JOB_EXECUTION_ID = ? " +
                    " ORDER by KEY_NAME asc";
            Object[] args = new Object[]{executionId};

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args);
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    list.add(new JobParameterTypeImpl((String) row.get("KEY_NAME"), (String) row.get("STRING_VAL"), (String) row.get("TYPE_CD")));
                }
            }
        } catch (Exception e) {

        }
        return list;
    }


    public Long findJobInstanceIdForJobExecutionId(Long jobExecutionId) {
        Long jobInstanceId = null;
        try {
            String sql = "SELECT JOB_INSTANCE_ID FROM BATCH_JOB_EXECUTION WHERE JOB_EXECUTION_ID = ?";
            Object[] args = new Object[]{jobExecutionId};

            List<Long> ids = (List<Long>) jdbcTemplate.query(sql, args, new RowMapper() {
                @Override
                public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

                    Long id = rs.getLong("JOB_INSTANCE_ID");
                    return id;
                }
            });
            if (ids != null) {
                jobInstanceId = ids.get(0);
            }
        } catch (Exception e) {

        }
        return jobInstanceId;
    }


}
