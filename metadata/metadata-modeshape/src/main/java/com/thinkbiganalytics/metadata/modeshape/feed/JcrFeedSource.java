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
import com.thinkbiganalytics.metadata.modeshape.catalog.dataset.JcrDataSet;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;

import javax.jcr.Node;

/**
 *
 */
public class JcrFeedSource extends JcrFeedConnection implements FeedSource {

    public static final String NODE_TYPE = "tba:feedSource";

    /**
     * boolean flag indicating this source entry is used as a sample and should not be used in feed lineage
     */
    public static final String IS_SAMPLE = "tba:isSample";

    public JcrFeedSource(Node node) {
        super(node);
    }

    public JcrFeedSource(Node node, JcrDatasource datasource) {
        super(node, datasource);
        datasource.addSourceNode(getNode());
    }

    public JcrFeedSource(Node node, JcrDataSet dataSet) {
        super(node, dataSet);
        dataSet.addSourceNode(getNode());
    }

    public void remove() {
        getDatasource()
            .map(JcrDatasource.class::cast)
            .ifPresent(src -> src.removeSourceNode(getNode()));
        getDataSet()
            .map(JcrDataSet.class::cast)
            .ifPresent(src -> src.removeSourceNode(getNode()));
        
        super.remove();
    }


    @Override
    public boolean isSample() {
        return getProperty(IS_SAMPLE);
    }

    public void setSample(boolean isSample) {
        setProperty(IS_SAMPLE, isSample);
    }
    
}
