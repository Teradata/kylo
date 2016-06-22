/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import java.io.Serializable;

/**
 *
 * @author Sean Felten
 */
public interface MetadataEventListener<E extends MetadataEvent<? extends Serializable>> {
    
    void notify(E event);

}
