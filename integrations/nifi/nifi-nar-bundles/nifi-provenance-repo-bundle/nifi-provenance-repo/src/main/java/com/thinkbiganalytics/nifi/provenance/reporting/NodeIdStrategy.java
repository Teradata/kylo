package com.thinkbiganalytics.nifi.provenance.reporting;

import org.apache.nifi.reporting.ReportingContext;

/**
 * Created by ru186002 on 18/01/2017.
 */
public interface NodeIdStrategy {

    String getNodeId(ReportingContext context);

}
