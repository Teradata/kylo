package com.thinkbiganalytics.metadata.modeshape;

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

import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategoryProvider;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasourceProvider;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedProvider;
import com.thinkbiganalytics.metadata.modeshape.tag.TagProvider;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplateProvider;

import org.springframework.context.annotation.Bean;

/**
 * Defines the beans used by JcrPropertyTest which override the mocks of JcrTestConfig.
 */
public class JcrPropertyTestConfig {

    @Bean
    public CategoryProvider categoryProvider() {
        return new JcrCategoryProvider();
    }

    @Bean
    public DatasourceProvider datasourceProvider() {
        return new JcrDatasourceProvider();
    }

    @Bean
    public FeedProvider feedProvider() {
        return new JcrFeedProvider();
    }

    @Bean
    public FeedManagerTemplateProvider feedManagerTemplateProvider() {
        return new JcrFeedTemplateProvider();
    }

    @Bean
    public TagProvider tagProvider() {
        return new TagProvider();
    }

    @Bean
    public JcrMetadataAccess metadata() {
        return new JcrMetadataAccess();
    }
}
