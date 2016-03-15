/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.op;

import java.io.Serializable;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 *
 * @author Sean Felten
 */
@SuppressWarnings("serial")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = HiveTablePartitions.class),
    @JsonSubTypes.Type(value = FileList.class),
    }
)
public class ChangeSet implements Serializable {

    private DateTime intrinsicTime;
    private String intrinsicPeriod;
    private double incompletenessFactor;

    public DateTime getIntrinsicTime() {
        return intrinsicTime;
    }

    public void setIntrinsicTime(DateTime intrinsicTime) {
        this.intrinsicTime = intrinsicTime;
    }

    public String getIntrinsicPeriod() {
        return intrinsicPeriod;
    }

    public void setIntrinsicPeriod(String intrinsicPeriod) {
        this.intrinsicPeriod = intrinsicPeriod;
    }

    public double getIncompletenessFactor() {
        return incompletenessFactor;
    }

    public void setIncompletenessFactor(double incompletenessFactor) {
        this.incompletenessFactor = incompletenessFactor;
    }

}
