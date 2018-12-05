package com.thinkbiganalytics.repository;

/*-
 * #%L
 * kylo-repository-service
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thinkbiganalytics.repository.filesystem.RepositoryMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
@ComponentScan(basePackages = {"com.thinkbiganalytics.repository", "com.thinkbiganalytics.repository.filesystem"})
public class RepositoryConfig {
    private static final Logger log = LoggerFactory.getLogger(RepositoryConfig.class);

    @Value("${expire.repository.cache:false}")
    boolean expireRepositoryCache;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ConversionService conversionService() {
        return new DefaultConversionService();
    }

    @Bean
    @Profile("!kyloUpgrade")
    public RepositoryMonitor repositoryMonitor() { return new RepositoryMonitor(); }

    @Bean
    public Cache<String, Boolean> templateUpdateInfoCache() {
        CacheBuilder builder = CacheBuilder.newBuilder();

        if(expireRepositoryCache){
            builder.expireAfterWrite(1, TimeUnit.HOURS);
            log.info("Template repository cache initialized with expiry of 1 hour");
        }

        return builder.build();
    }

    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper();
    }
}
