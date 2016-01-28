/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset;

import java.util.List;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.dataset.ChangeSet;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;

/**
 *
 * @author Sean Felten
 */
public class CoreDataset implements Dataset {

    private ID id;
    private String name;
    private String description;
    private DateTime creationTime;
    private List<ChangeSet<?, ?>> changeSets;

    public ID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public List<ChangeSet<?, ?>> getChangeSets() {
        return changeSets;
    }

}
