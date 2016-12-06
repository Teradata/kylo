/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import java.io.Serializable;
import java.security.Principal;

import org.joda.time.DateTime;

/**
 *
 * @author Sean Felten
 */
public interface MetadataEvent<C extends Serializable> extends Serializable {

    DateTime getTimestamp();
    
    Principal getUserPrincipal();
    
    C getData();
}
