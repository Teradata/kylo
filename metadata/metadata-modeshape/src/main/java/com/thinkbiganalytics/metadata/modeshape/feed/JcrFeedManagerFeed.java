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

import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.category.JcrFeedManagerCategory;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;

import javax.jcr.Node;

/**
 */
public class JcrFeedManagerFeed<C extends JcrFeedManagerCategory> extends JcrFeed<C> implements FeedManagerFeed<C> {

//    public static String FEED_JSON = "tba:json";
//    public static String PROCESS_GROUP_ID = "tba:processGroupId";
//    public static String FEED_TEMPLATE = "tba:feedTemplate";


    public JcrFeedManagerFeed(Node node) {
        super(node);
    }

    public JcrFeedManagerFeed(Node node, JcrCategory category) {
        super(node, category);
    }

    @Override
    public String getJson() {
        return getFeedDetails().map(d -> d.getJson()).orElse(null);
    }

    @Override
    public void setJson(String json) {
        getFeedDetails().ifPresent(d -> d.setJson(json));
    }

    @Override
    public FeedManagerTemplate getTemplate() {
        return getFeedDetails().map(d -> d.getTemplate()).orElse(null);
    }

    @Override
    public void setTemplate(FeedManagerTemplate template) {
        getFeedDetails().ifPresent(d -> d.setTemplate(template));
    }

    @Override
    public String getNifiProcessGroupId() {
        return getFeedDetails().map(d -> d.getNifiProcessGroupId()).orElse(null);
    }

    public void setNifiProcessGroupId(String id) {
        getFeedDetails().ifPresent(d -> d.setNifiProcessGroupId(id));
    }

    public void setVersion(Integer version) {

    }


    public C getCategory() {
        return getFeedSummary().map(s -> (C) s.getCategory(JcrFeedManagerCategory.class)).orElse(null);
    }

}
