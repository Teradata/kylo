/**
 * 
 */
package com.thinkbiganalytics.server.upgrade;

/*-
 * #%L
 * kylo-operational-metadata-upgrade-service
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

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

import com.thinkbiganalytics.metadata.jpa.app.JpaKyloVersionConfig;
import com.thinkbiganalytics.metadata.modeshape.MetadataJcrConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeAuthConfig;
import com.thinkbiganalytics.server.upgrade.liquibase.LiquibaseConfiguration;

import liquibase.integration.spring.SpringLiquibase;

/**
 * The configuration for Kylo's upgrade service.
 */
@Configuration
@Import(LiquibaseConfiguration.class)
public class KyloUpgradeConfig {

    @Inject
    @SuppressWarnings("unused")
    private SpringLiquibase liquibase;
    
    @Configuration
    @ComponentScan(basePackages="com.thinkbiganalytics")
    @Import({ ModeShapeEngineConfig.class, MetadataJcrConfig.class, ModeShapeAuthConfig.class, JpaKyloVersionConfig.class })
    public static class UpgradeStateConfig {
        
        @Bean
        @DependsOn("liquibase")
        public KyloUpgradeService upgradeService() {
            return new KyloUpgradeService();
        }

    }
}
