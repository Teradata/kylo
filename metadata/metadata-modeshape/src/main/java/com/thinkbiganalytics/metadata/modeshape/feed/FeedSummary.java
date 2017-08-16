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

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryNotFoundException;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;

import java.util.Optional;

import javax.jcr.Node;

public class FeedSummary extends AbstractJcrAuditableSystemEntity {

    public static final String NODE_TYPE = "tba:feedSummary";

    public static final String DETAILS = "tba:details";

    public static final String CATEGORY = "tba:category";

    private FeedDetails details;
    private JcrFeed feed;

    public FeedSummary(Node node, JcrFeed feed) {
        super(JcrVersionUtil.createAutoCheckoutProxy(node));
        this.feed = feed;
    }

    public FeedSummary(Node node, JcrCategory category, JcrFeed feed) {
        this(node, feed);
        if (category != null) {
            setProperty(CATEGORY, category);
        }
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

    protected Category getCategory(Class<? extends JcrCategory> categoryClass) {
        Category category = null;
        try {
            category = (Category) getProperty(CATEGORY, categoryClass);
        } catch (Exception e) {
            if (category == null) {
                try {
                    category = (Category) JcrUtil.constructNodeObject(this.feed.getNode().getParent(), categoryClass, null);
                } catch (Exception e2) {
                    throw new CategoryNotFoundException("Unable to find category on Feed for category type  " + categoryClass + ". Exception: " + e.getMessage(), null);
                }
            }
        }
        if (category == null) {
            throw new CategoryNotFoundException("Unable to find category on Feed ", null);
        }
        return category;
    }

    protected JcrFeed getParentFeed() {
        return this.feed;
    }
}
