package com.thinkbiganalytics.metadata.modeshape.sla;

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

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;

import java.util.Set;

import javax.jcr.Node;

/**
 * Resulting Query object that gets all SLAs and their related Feeds
 *
 * @see JcrFeedServiceLevelAgreementProvider
 */
public class JcrFeedServiceLevelAgreement extends JcrServiceLevelAgreement implements FeedServiceLevelAgreement {

    private Set<JcrFeed> feeds;


    public JcrFeedServiceLevelAgreement(Node node, Set<JcrFeed> feeds) {
        super(node);
        this.feeds = feeds;
    }

    @Override
    public Set<? extends Feed> getFeeds() {
        return feeds;
    }
}
