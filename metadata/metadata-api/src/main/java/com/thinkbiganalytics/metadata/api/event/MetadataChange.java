/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import java.io.Serializable;
import java.util.Objects;

/**
 * Base type describing the metadata change as part a metadata event.  Subclasses should provide the details of the change.
 * @author Sean Felten
 */
public class MetadataChange implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum ChangeType { CREATE, UPDATE, DELETE }

    private final ChangeType change;
    private final String description;
    
    public MetadataChange(ChangeType change) {
        this(change, "");
    }
    
    public MetadataChange(ChangeType change, String descr) {
        super();
        this.change = change;
        this.description = descr;
    }

    public ChangeType getChange() {
        return change;
    }

    public String getDescription() {
        return description;
    }
    

    @Override
    public int hashCode() {
        return Objects.hash(this.change, this.description);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetadataChange) {
            MetadataChange that = (MetadataChange) obj;
            return Objects.equals(this.change, this.change) &&
                   Objects.equals(this.description, that.description);
        } else {
            return false;
        }
    }

}
