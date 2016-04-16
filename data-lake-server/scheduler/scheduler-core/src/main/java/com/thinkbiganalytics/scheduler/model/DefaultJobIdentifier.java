package com.thinkbiganalytics.scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thinkbiganalytics.scheduler.JobIdentifier;

/**
 * Created by sr186054 on 9/23/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultJobIdentifier extends DefaultScheduleIdentifier implements JobIdentifier {

  public DefaultJobIdentifier() {
    super();
  }

  public DefaultJobIdentifier(@JsonProperty("name") String name, @JsonProperty("group") String group) {
    super(name, group);
  }
}
