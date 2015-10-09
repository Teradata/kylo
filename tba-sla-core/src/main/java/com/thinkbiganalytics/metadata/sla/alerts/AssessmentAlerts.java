/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.alerts;

import java.net.URI;

import com.thinkbiganalytics.alerts.spi.AlertDescriptor;

/**
 *
 * @author Sean Felten
 */
public interface AssessmentAlerts {
    static final AlertDescriptor VIOLATION_ALERT = new AlertDescriptor(URI.create("urn:tba:alert:sla::violation"),
                                                                       "application/x-java-serialized-object",
                                                                       "Violation of a service level agreement",
                                                                       true);
}
