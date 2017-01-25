package com.thinkbiganalytics.metadata.sla.config;

/*-
 * #%L
 * thinkbig-sla-email
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

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by sr186054 on 8/9/16.
 */
@Configuration
@Profile("developer.email")
@PropertySource("classpath:/conf/sla.email.properties")
public class DeveloperEmailConfiguration {


/*
        @Bean(name = "emailProperties")
        public PropertyPlaceholderConfigurer jiraPropertiesConfigurer() {
            PropertyPlaceholderConfigurer configurer = new
                PropertyPlaceholderConfigurer();
            configurer.setLocations(new ClassPathResource("/conf/sla.email.properties"));
            configurer.setIgnoreUnresolvablePlaceholders(true);
            configurer.setIgnoreResourceNotFound(true);
            return configurer;
        }
*/


}
