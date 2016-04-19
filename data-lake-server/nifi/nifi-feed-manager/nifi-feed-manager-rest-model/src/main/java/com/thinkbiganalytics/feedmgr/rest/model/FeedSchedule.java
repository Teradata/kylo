package com.thinkbiganalytics.feedmgr.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.feedmgr.metadata.MetadataField;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessorSchedule;

import java.util.List;

/**
 * Created by sr186054 on 2/22/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedSchedule extends NifiProcessorSchedule {

    List<GenericUIPrecondition> preconditions;

    @MetadataField
    String dependentFeedNames;

    public List<GenericUIPrecondition> getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(List<GenericUIPrecondition> preconditions) {
        this.preconditions = preconditions;
    }

    public String getDependentFeedNames() {
        return dependentFeedNames;
    }

    public void setDependentFeedNames(String dependentFeedNames) {
        this.dependentFeedNames = dependentFeedNames;
    }

    @JsonIgnore
    public void updateDependentFeedNamesString(){
        StringBuffer sb = new StringBuffer();
        if(preconditions != null) {


            for(GenericUIPrecondition precondition :preconditions){
                if(!sb.toString().equalsIgnoreCase("")){
                    sb.append(",");
                }
                sb.append(precondition.getDependentFeedName());
            }
        }
              setDependentFeedNames(sb.toString());

    }
}
