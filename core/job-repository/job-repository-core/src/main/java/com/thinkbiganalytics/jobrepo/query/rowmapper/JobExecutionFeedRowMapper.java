package com.thinkbiganalytics.jobrepo.query.rowmapper;

import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr186054 on 8/13/15.
 */
public class JobExecutionFeedRowMapper extends JobExecutionRowMapper {

  @Override
  protected JobParameters fetchJobParameters(ResultSet rs) {
    JobParameters jobParameters = null;
    try {
      String feedName = rs.getString("FEED_NAME");
      Map<String, JobParameter> params = new HashMap<String, JobParameter>();

      JobParameter jobParameter = new JobParameter(feedName, true);
      params.put(FeedConstants.PARAM__FEED_NAME, jobParameter);
      jobParameters = new JobParameters(params);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return jobParameters;

  }
}
