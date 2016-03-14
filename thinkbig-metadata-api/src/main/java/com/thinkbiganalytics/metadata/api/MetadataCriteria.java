/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

/**
 *
 * @author Sean Felten
 */
public interface MetadataCriteria<C extends MetadataCriteria<C>> {

    C limit(int size);
}
