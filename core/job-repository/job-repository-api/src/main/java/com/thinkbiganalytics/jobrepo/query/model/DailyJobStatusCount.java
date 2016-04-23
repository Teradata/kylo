package com.thinkbiganalytics.jobrepo.query.model;

import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitution;

import java.util.List;

/**
 * Created by sr186054 on 4/14/16.
 */
public interface DailyJobStatusCount {

  DatabaseQuerySubstitution.DATE_PART getDatePart();

  void setDatePart(DatabaseQuerySubstitution.DATE_PART datePart);

  Integer getInterval();

  void setInterval(Integer interval);

  List<JobStatusCount> getJobStatusCounts();

  void setJobStatusCounts(List<JobStatusCount> jobStatusCounts);

  void checkAndAddStartDate();
}
