package com.thinkbiganalytics.nifi.provenance.reporting;

/*-
 * #%L
 * thinkbig-nifi-provenance-repo-v1
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.nifi.reporting.ReportingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NodeIdStrategyV1 implements NodeIdStrategy {

    private static final Logger log = LoggerFactory.getLogger(NodeIdStrategyV1.class);

    @Override
    public String getNodeId(ReportingContext context) {
        final boolean isClustered = context.isClustered();
        final String nodeId = isClustered ? context.getClusterNodeIdentifier() : "non-clustered-node-id";
        if (nodeId == null) {
            log.info("This instance of NiFi is configured for clustering, but the Cluster Node Identifier is not yet available. "
                    + "Will wait for Node Identifier to be established.");
        }
        return nodeId;
    }
}
