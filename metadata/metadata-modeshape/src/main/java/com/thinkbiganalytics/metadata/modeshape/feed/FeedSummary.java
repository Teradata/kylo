package com.thinkbiganalytics.metadata.modeshape.feed;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.AuditableMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.IndexControlledMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.SystemEntityMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.TaggableMixin;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;

import java.util.Optional;

import javax.jcr.Node;

public class FeedSummary extends JcrObject implements SystemEntityMixin, AuditableMixin, TaggableMixin, IndexControlledMixin {

    public static final String NODE_TYPE = "tba:feedSummary";

    public static final String DETAILS = "tba:details";

    private FeedDetails details;
    private JcrFeed feed;

    public FeedSummary(Node node, JcrFeed feed) {
        super(JcrVersionUtil.createAutoCheckoutProxy(node));
        this.feed = feed;
    }

    public FeedSummary(Node node, JcrCategory category, JcrFeed feed) {
        this(node, feed);
    }

    public Optional<FeedDetails> getFeedDetails() {
        if (this.details == null) {
            if (JcrUtil.hasNode(getNode(), DETAILS)) {
                this.details = JcrUtil.getJcrObject(getNode(), DETAILS, FeedDetails.class, this);
                return Optional.of(this.details);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.of(this.details);
        }
    }

    protected JcrFeed getParentFeed() {
        return this.feed;
    }
}
