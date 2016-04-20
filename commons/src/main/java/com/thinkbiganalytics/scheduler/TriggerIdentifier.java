package com.thinkbiganalytics.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sr186054 on 9/23/15.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class TriggerIdentifier extends ScheduleIdentifier {

    public TriggerIdentifier() {
        super();
    }
    public TriggerIdentifier(@JsonProperty("name")String name, @JsonProperty("group")String group) {
        super(name, group);
    }
}
