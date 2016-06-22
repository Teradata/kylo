/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 *
 * @author Sean Felten
 */
public interface EventMatcher<E extends MetadataEvent<? extends Serializable>> extends Predicate<E> {

    boolean test(E event);
}
