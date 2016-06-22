/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import java.io.Serializable;

/**
 *
 * @author Sean Felten
 */
public interface MetadataEventService {

    <E extends MetadataEvent<? extends Serializable>> void notify(E event);
    
    
    <E extends MetadataEvent<? extends Serializable>> void addListener(MetadataEventListener<E> listener);
    
    <E extends MetadataEvent<? extends Serializable>> void addListener(MetadataEventListener<E> listener, 
                                                                       EventMatcher<E> matcher);
    
    void removeListener(MetadataEventListener<?> listener);
}
