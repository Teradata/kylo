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
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreementRelationship;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensibleEntity;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.util.Set;

import javax.jcr.Node;

/**
 * @see JcrFeedServiceLevelAgreementProvider#relate(ServiceLevelAgreement, Set)
 */
public class JcrFeedServiceLevelAgreementRelationship extends JcrExtensibleEntity implements FeedServiceLevelAgreementRelationship {


    public static final String NODE_TYPE = "tba:slaFeedsRelationship";
    public static final String FEEDS = "tba:feeds"; /// list of feed references on the SLA


    public JcrFeedServiceLevelAgreementRelationship(Node node) {
        super(node);
    }

    @Override
    public FeedServiceLevelAgreement getAgreement() {
        Node node = (Node) JcrUtil.getParent(getNode());
        return new JcrFeedServiceLevelAgreement(node, getJcrFeeds());
    }

    @Override
    public Set<? extends Feed> getFeeds() {
        return getJcrFeeds();
    }

    @Override
    public boolean removeFeedRelationships(ServiceLevelAgreement.ID id) {
        @SuppressWarnings("unchecked")
        final Set<JcrFeed> feeds = (Set<JcrFeed>) getFeeds();
        if (feeds != null && !feeds.isEmpty()) {
            feeds.stream()
                .filter(feed -> feed != null)
                .forEach(feed -> feed.removeServiceLevelAgreement(id));
        }
        setProperty(FEEDS, null);
        return true;


    }

    private Set<JcrFeed> getJcrFeeds() {
        return getPropertyAsSet(FEEDS, JcrFeed.class);
    }
}
