package com.thinkbiganalytics.nifi.provenance.reporting;

import org.apache.nifi.reporting.ReportingContext;
import org.springframework.stereotype.Component;

/**
 * Created by ru186002 on 18/01/2017.
 */
@Component
public class NodeIdStrategyV0 implements NodeIdStrategy {

    @Override
    public String getNodeId(ReportingContext context) {
        return "non-clustered-node-id";
    }
}
