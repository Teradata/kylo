package com.thinkbiganalytics.nifi.provenance.reporting;

import org.apache.nifi.reporting.ReportingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by ru186002 on 18/01/2017.
 */
@Component
public class NodeIdStrategyV1 implements NodeIdStrategy {

    private static final Logger log = LoggerFactory.getLogger(NodeIdStrategyV1.class);

    @Override
    public String getNodeId(ReportingContext context) {
        final boolean isClustered = context.isClustered();
        final String nodeId = context.getClusterNodeIdentifier();
        if (nodeId == null && isClustered) {
            log.info("This instance of NiFi is configured for clustering, but the Cluster Node Identifier is not yet available. "
                    + "Will wait for Node Identifier to be established.");
        }
        return nodeId;
    }
}
