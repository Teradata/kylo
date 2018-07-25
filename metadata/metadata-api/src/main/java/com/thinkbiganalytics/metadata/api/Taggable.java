/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

import java.util.Set;

/**
 * Marks an entity a able to support tags.
 */
public interface Taggable {

    /**
     * @param tag the tag to check
     * @return true if the tag exists
     */
    boolean hasTag(String tag);
    
    /**
     * @return the current tags
     */
    Set<String> getTags();
    
    /**
     * @param tag the new tag
     * @return the current state of the tags after the add
     */
    Set<String> addTag(String tag);
    
    /**
     * @param tag the tag to remove
     * @return
     */
    Set<String> removeTag(String tag);
}
