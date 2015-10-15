/**
 * 
 */
package com.thinkbiganalytics.alerts.sla;

import java.net.URI;

import com.thinkbiganalytics.alerts.spi.AlertDescriptor;

/**
 *
 * @author Sean Felten
 */
public interface AssessmentAlerts {
    
    static final URI VIOLATION_ALERT_TYPE = URI.create("urn:alert:sla::violation");
    
    static final AlertDescriptor VIOLATION_ALERT = new AlertDescriptor(VIOLATION_ALERT_TYPE,
                                                                       "application/x-java-serialized-object",
                                                                       "Violation of a service level agreement",
                                                                       true);
}
