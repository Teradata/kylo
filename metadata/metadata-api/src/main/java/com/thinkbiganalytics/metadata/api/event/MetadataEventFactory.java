/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import java.io.Serializable;

/**
 *
 * @author Sean Felten
 */
public interface MetadataEventFactory {

    <E extends MetadataEvent<? extends Serializable>> boolean isCreator(Class<E> evType);
    
    <C extends Serializable, E extends MetadataEvent<C>> E createEvent(C content, Class<E> evType);
}
