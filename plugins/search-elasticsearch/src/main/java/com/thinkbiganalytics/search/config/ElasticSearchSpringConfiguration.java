package com.thinkbiganalytics.search.config;

/*-
 * #%L
 * kylo-search-elasticsearch
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

import com.thinkbiganalytics.search.ElasticSearchModeShapeConfigurationService;
import com.thinkbiganalytics.search.ElasticSearchService;
import com.thinkbiganalytics.search.api.RepositoryIndexConfiguration;
import com.thinkbiganalytics.search.api.Search;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile("search-es")
@PropertySource("classpath:elasticsearch.properties")
public class ElasticSearchSpringConfiguration {

    @Bean
    @ConfigurationProperties("search")
    public ElasticSearchClientConfiguration elasticSearchClientConfiguration() {
        return new ElasticSearchClientConfiguration();
    }

    @Bean
    @Profile("!kyloUpgrade")
    public RepositoryIndexConfiguration elasticSearchModeShapeConfigurationService(ElasticSearchClientConfiguration elasticSearchClientConfiguration) {
        return new ElasticSearchModeShapeConfigurationService(elasticSearchClientConfiguration);
    }

    @Bean
    public Search search(ElasticSearchClientConfiguration elasticSearchClientConfiguration) {
        return new ElasticSearchService(elasticSearchClientConfiguration);
    }

}
