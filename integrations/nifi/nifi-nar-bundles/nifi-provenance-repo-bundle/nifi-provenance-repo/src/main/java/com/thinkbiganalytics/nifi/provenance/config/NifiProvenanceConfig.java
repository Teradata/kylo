package com.thinkbiganalytics.nifi.provenance.config;

/*-
 * #%L
 * thinkbig-nifi-provenance-repo
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

import com.thinkbiganalytics.nifi.provenance.jms.ProvenanceEventJmsWriter;
import com.thinkbiganalytics.nifi.provenance.repo.ConfigurationPropertiesRefresher;
import com.thinkbiganalytics.nifi.provenance.util.SpringApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

;

/**
 * Spring bean configuration for Kylo NiFi Provenance
 */
@Configuration
public class NifiProvenanceConfig {

    private static final Logger log = LoggerFactory.getLogger(NifiProvenanceConfig.class);

    @Bean
    public SpringApplicationContext springApplicationContext() {
        return new SpringApplicationContext();
    }


    @Bean
    public ProvenanceEventJmsWriter provenanceEventActiveMqWriter() {
        return new ProvenanceEventJmsWriter();
    }

    @Bean
    public ConfigurationPropertiesRefresher configurationPropertiesRefresher(){
        return new ConfigurationPropertiesRefresher();
    }

}
