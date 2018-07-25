/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

/**
 *
 */
public interface IndexControlled {

    boolean isAllowIndexing();
    
    void setAllowIndexing(boolean allowIndexing);
}
