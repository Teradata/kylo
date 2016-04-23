package com.thinkbiganalytics.scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thinkbiganalytics.scheduler.TriggerIdentifier;

/**
 * Created by sr186054 on 9/23/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultTriggerIdentifier extends DefaultScheduleIdentifier implements TriggerIdentifier {

  public DefaultTriggerIdentifier() {
    super();
  }

  public DefaultTriggerIdentifier(@JsonProperty("name") String name, @JsonProperty("group") String group) {
    super(name, group);
  }
}
