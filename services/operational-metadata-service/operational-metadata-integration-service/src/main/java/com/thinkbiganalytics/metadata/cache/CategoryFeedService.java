package com.thinkbiganalytics.metadata.cache;
/*-
 * #%L
 * thinkbig-job-repository-controller
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
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.service.category.SimpleCategoryCache;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.jpa.common.EntityAccessControlled;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Service to get Feeds Grouped by Category using Access Control
 */
public class CategoryFeedService {

    @Inject
    OpsManagerFeedProvider opsManagerFeedProvider;

    @Inject
    SimpleCategoryCache categoryCache;

    @EntityAccessControlled
    public Map<FeedCategory, List<SimpleFeed>> getFeedsByCategory(){
        Map<String,List<OpsManagerFeed>> categoryFeeds = opsManagerFeedProvider.getFeedsGroupedByCategory();
        Map<String,FeedCategory> feedCategoryMap = categoryCache.getCategoriesByName();
        Map<FeedCategory,List<SimpleFeed>> feedCategoryListMap = categoryFeeds.entrySet().stream().filter(e -> feedCategoryMap.containsKey(e.getKey())).collect(Collectors.toMap(e -> feedCategoryMap.get(e.getKey()),e -> e.getValue().stream().map(f -> new SimpleFeed(f.getId().toString(),f.getName(),feedCategoryMap.get(e.getKey()))).collect(Collectors.toList())));
        return feedCategoryListMap;
    }

    public static class SimpleFeed {
        private String name;
        private String id;
        private FeedCategory category;

        public SimpleFeed(String id, String name, FeedCategory category) {
            this.name = name;
            this.id = id;
            this.category = category;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public FeedCategory getCategory() {
            return category;
        }
    }
}
