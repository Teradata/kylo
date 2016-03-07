/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.op;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTablePartition;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HiveTablePartitions extends ChangeSet {

    List<HiveTablePartition> partions = new ArrayList<>();

    public List<HiveTablePartition> getPartions() {
        return partions;
    }

    public void setPartions(List<HiveTablePartition> partions) {
        this.partions = partions;
    }
    
    
}
