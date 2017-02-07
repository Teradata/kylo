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

    public static String FEED_JSON = "tba:json";
    public static String PROCESS_GROUP_ID = "tba:processGroupId";
    public static String FEED_TEMPLATE = "tba:feedTemplate";


    public JcrFeedManagerFeed(Node node) {
        super(node);
    }

    public JcrFeedManagerFeed(Node node, JcrCategory category) {
        super(node, category);
    }

    @Override
    public void setTemplate(FeedManagerTemplate template) {
        setProperty(FEED_TEMPLATE, template);
        template.addFeed(this);
    }

    @Override
    public String getJson() {
        return getProperty(FEED_JSON, String.class);
    }

    @Override
    public void setJson(String json) {
        setProperty(FEED_JSON, json);
    }

    @Override
    public FeedManagerTemplate getTemplate() {
        return getProperty(FEED_TEMPLATE, JcrFeedTemplate.class, true);
    }

    @Override
    public String getNifiProcessGroupId() {
        return getProperty(PROCESS_GROUP_ID, String.class);
    }

    public void setNifiProcessGroupId(String id) {
        setProperty(PROCESS_GROUP_ID, id);
    }

    public void setVersion(Integer version) {

    }


    public C getCategory() {

        return (C) getCategory(JcrFeedManagerCategory.class);
    }

}
