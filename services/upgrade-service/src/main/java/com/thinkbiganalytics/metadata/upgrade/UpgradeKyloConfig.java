/**
 * 
 */
package com.thinkbiganalytics.metadata.upgrade;

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
import org.springframework.context.annotation.Profile;

import com.thinkbiganalytics.liquibase.LiquibaseConfiguration;
import com.thinkbiganalytics.metadata.jpa.app.JpaKyloVersionConfig;
import com.thinkbiganalytics.metadata.modeshape.MetadataJcrConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeAuthConfig;

import liquibase.integration.spring.SpringLiquibase;

/**
 * The configuration for Kylo's upgrade service.
 */
@Configuration
@Import(LiquibaseConfiguration.class)
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class UpgradeKyloConfig {

    @Inject
    @SuppressWarnings("unused")
    private SpringLiquibase liquibase;
    
    @Configuration
    @ComponentScan(basePackages="com.thinkbiganalytics")
    @Import({ ModeShapeEngineConfig.class, MetadataJcrConfig.class, ModeShapeAuthConfig.class, JpaKyloVersionConfig.class })
    public static class UpgradeStaeConfig {
        
        public UpgradeStaeConfig() {
            super();
        }
        
        @Bean
        @DependsOn("liquibase")
        public UpgradeKyloService upgradeService() {
            return new UpgradeKyloService();
        }

    }
//    
//    @Inject
//    private Environment environment;
//    
//    @Bean
//    @Primary
//    public MetadataJcrConfigurator jcrConfigurator(List<PostMetadataConfigAction> postConfigActions) {
//        // Overrides the this bean from MetadataJcrConfig so that it does not invoke configure() at bean construction.
//        return new MetadataJcrConfigurator(postConfigActions);
//    }
//
//    @Bean
//    @Primary
//    public RepositoryConfiguration metadataRepoConfig() throws IOException {
//        for (String prop : ModeShapeEngineConfig.CONFIG_PROPS) {
//            if (this.environment.containsProperty(prop)) {
//                System.setProperty(prop, this.environment.getProperty(prop));
//            }
//        }
//
//        KyloVersion version = upgradeService().getCurrentVersion();
//        URL configUrl = upgradeService().getUpgradeState(version)
//                        .map(upgrade -> upgrade.getResource("/metadata-repository.json"))
//                        .orElse(new ClassPathResource("/metadata-repository.json").getURL());
//        RepositoryConfiguration config = RepositoryConfiguration.read(configUrl);
//
//        Problems problems = config.validate();
//        if (problems.hasErrors()) {
//            log.error("Problems with the ModeShape repository configuration: \n{}", problems);
//            throw new RuntimeException("Problems with the ModeShape repository configuration: " + problems);
//        }
//
//        return config;
//    }
}
