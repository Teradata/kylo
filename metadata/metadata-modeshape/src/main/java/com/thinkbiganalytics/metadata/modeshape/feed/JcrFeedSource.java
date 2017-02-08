/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.feed;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import javax.jcr.Node;

/**
 *
 */
public class JcrFeedSource extends JcrFeedConnection implements FeedSource {

    public static final String NODE_TYPE = "tba:feedSource";

    public JcrFeedSource(Node node) {
        super(node);
    }

    public JcrFeedSource(Node node, JcrDatasource datasource) {
        super(node, datasource);
        datasource.addSourceNode(this.node);
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedSource#getAgreement()
     */
    @Override
    public ServiceLevelAgreement getAgreement() {
        return null;
    }
}
