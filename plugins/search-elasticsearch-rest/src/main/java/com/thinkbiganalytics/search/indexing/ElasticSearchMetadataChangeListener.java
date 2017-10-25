package com.thinkbiganalytics.search.indexing;

import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.service.category.MetadataChangeListener;
import com.thinkbiganalytics.search.api.Search;
import com.thinkbiganalytics.search.api.SearchIndex;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.inject.Inject;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

public class ElasticSearchMetadataChangeListener implements MetadataChangeListener {

    private static Logger log = LoggerFactory.getLogger(ElasticSearchMetadataChangeListener.class);

    @Inject
    private Search elasticSearchRestService;

    @Override
    public void categoryCreatedOrUpdated(FeedCategory category) {

        try {
            Map<String, Object> categoryMap = PropertyUtils.describe(category);
            elasticSearchRestService.index(SearchIndex.DATA, "feed-category", category.getSystemName(), categoryMap);
        } catch (Exception e) {
            log.error("Error describing category", e);
        }

    }

    @Override
    public void feedCreatedOrUpdated(FeedMetadata feed) {
        try {
            Map<String, Object> feedMap = PropertyUtils.describe(feed);
            elasticSearchRestService.index(SearchIndex.DATA, "feed-metadata", feed.getSystemFeedName(), feedMap, feed.getSystemCategoryName());
        } catch (Exception e) {
            log.error("Error describing feed", e);
        }
    }
}
