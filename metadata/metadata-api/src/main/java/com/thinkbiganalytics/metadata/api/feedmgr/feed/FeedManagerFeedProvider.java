package com.thinkbiganalytics.metadata.api.feedmgr.feed;

/*-
 * #%L
 * thinkbig-metadata-api
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

import com.thinkbiganalytics.metadata.api.BaseProvider;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;

import java.util.List;

/**
 */
public interface FeedManagerFeedProvider extends BaseProvider<FeedManagerFeed, Feed.ID> {

    FeedManagerFeed findBySystemName(String categorySystemName, String systemName);

    List<? extends FeedManagerFeed> findByTemplateId(FeedManagerTemplate.ID templateId);

    List<? extends FeedManagerFeed> findByCategoryId(FeedManagerCategory.ID categoryId);

    FeedManagerFeed ensureFeed(Feed feed);

    FeedManagerFeed ensureFeed(Category.ID categoryId, String feedSystemName);


}
