/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;

/**
 *
 * @author Sean Felten
 */
public class BaseDataset implements Dataset {

    private ID id;
    private String name;
    private String description;
    private DateTime creationTime;
    private List<ChangeSet<?, ?>> changeSets = new ArrayList<>();

    public BaseDataset(String name, String descr) {
        this.id = new DatasetId();
        this.creationTime = new DateTime();
        this.name = name;
        this.description = descr;
    }

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

    
    private static class DatasetId implements ID {
        private UUID uuid = UUID.randomUUID();
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DatasetId) {
                DatasetId that = (DatasetId) obj;
                return Objects.equals(this.uuid, that.uuid);
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.uuid);
        }
        
        @Override
        public String toString() {
            return this.uuid.toString();
        }
    }
}
