/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 * FOR TESTING ONLY.  This metric will always be assessed with the result it has been set on it.
 * @author Sean Felten
 */
public abstract class TestMetric implements Metric {

    private static final long serialVersionUID = 1L;
    
    private AssessmentResult result;
    private String message;
    
    public TestMetric() {
    }
    
    public TestMetric(AssessmentResult result) {
        this(result, "This metric will always assess with the result: " + result);
    }

    public TestMetric(AssessmentResult result, String msg) {
        super();
        this.result = result;
        this.message = msg;
    }
    
    public AssessmentResult getResult() {
        return result;
    }
    
    public void setResult(AssessmentResult result) {
        this.result = result;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }
}
