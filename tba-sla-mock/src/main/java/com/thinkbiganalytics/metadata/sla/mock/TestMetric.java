/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.mock;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public class TestMetric implements Metric {
    
    private boolean success;
    private String description;
    
    public TestMetric(boolean success, String descr) {
        this.success = success;
        this.description = descr;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.Metric#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }
    
    public boolean isSuccess() {
        return success;
    }

}
