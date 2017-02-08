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

import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;

import javax.jcr.Node;

/**
 *
 */
public class JcrFeedDestination extends JcrFeedConnection implements FeedDestination {

    public static final String NODE_TYPE = "tba:feedDestination";

    /**
     * @param node
     */
    public JcrFeedDestination(Node node) {
        super(node);
    }

    /**
     * @param node
     * @param datasource
     */
    public JcrFeedDestination(Node node, JcrDatasource datasource) {
        super(node, datasource);
        datasource.addDestinationNode(this.node);
    }
}
