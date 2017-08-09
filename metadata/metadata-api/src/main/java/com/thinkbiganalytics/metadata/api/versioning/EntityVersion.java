/**
 * 
 */
package com.thinkbiganalytics.metadata.api.versioning;

import java.io.Serializable;
import java.util.Optional;

import org.joda.time.DateTime;

/**
 *
 */
public interface EntityVersion<E> {
    
    ID getId();

    String getName();
    
    DateTime getCreatedDate();
    
    Optional<E> getEntity();
    

    interface ID extends Serializable {

    }
}
