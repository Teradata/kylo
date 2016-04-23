package com.thinkbiganalytics.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by sr186054 on 4/14/16.
 */
public interface ScheduleIdentifier extends Comparable<ScheduleIdentifier>{

  String getName();

  String getGroup();

  @JsonIgnore
  String getUniqueName();
}
