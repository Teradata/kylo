/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.op;

import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dataset {

    enum ChangeType {
        UPDATE, DELETE
    }

    private DateTime getTime;
    private ChangeType getType;
    private List<ChangeSet> changeSets;

    public DateTime getGetTime() {
        return getTime;
    }

    public void setGetTime(DateTime getTime) {
        this.getTime = getTime;
    }

    public ChangeType getGetType() {
        return getType;
    }

    public void setGetType(ChangeType getType) {
        this.getType = getType;
    }

    public List<ChangeSet> getChangeSets() {
        return changeSets;
    }

    public void setChangeSets(List<ChangeSet> changeSets) {
        this.changeSets = changeSets;
    }

}
