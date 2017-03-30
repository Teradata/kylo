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

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.RepositoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.api.app.KyloVersion;
import com.thinkbiganalytics.metadata.jpa.app.JpaKyloVersionConfig;
import com.thinkbiganalytics.metadata.modeshape.MetadataJcrConfig;
import com.thinkbiganalytics.metadata.modeshape.MetadataJcrConfigurator;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;

/**
 * The configuration for Kylo's upgrade service.
 */
@Configuration
@ComponentScan(basePackages="com.thinkbiganalytics")
@Import({ ModeShapeEngineConfig.class, MetadataJcrConfig.class, JpaKyloVersionConfig.class })
public class UpgradeKyloConfig {
    
    private static final Logger log = LoggerFactory.getLogger(UpgradeKyloConfig.class);

    @Inject
    private Environment environment;

    
    @Bean
    public UpgradeKyloService upgradeService() {
        return new UpgradeKyloService();
    }
    
    @Bean
    public KyloUpgrader upgrader() {
        return new KyloUpgrader();
    }
    
    @Bean
    @Primary
    public MetadataJcrConfigurator jcrConfigurator(List<PostMetadataConfigAction> postConfigActions) {
        // Overrides the this bean from MetadataJcrConfig so that it does not invoke configure() at bean construction.
        return new MetadataJcrConfigurator(postConfigActions);
    }

    @Bean
    @Primary
    public RepositoryConfiguration metadataRepoConfig() throws IOException {
        for (String prop : ModeShapeEngineConfig.CONFIG_PROPS) {
            if (this.environment.containsProperty(prop)) {
                System.setProperty(prop, this.environment.getProperty(prop));
            }
        }

        KyloVersion version = upgradeService().getCurrentVersion();
        URL configUrl = upgradeService().getUpgradeState(version)
                        .map(upgrade -> upgrade.getResource("/metadata-repository.json"))
                        .orElse(new ClassPathResource("/metadata-repository.json").getURL());
        RepositoryConfiguration config = RepositoryConfiguration.read(configUrl);

        Problems problems = config.validate();
        if (problems.hasErrors()) {
            log.error("Problems with the ModeShape repository configuration: \n{}", problems);
            throw new RuntimeException("Problems with the ModeShape repository configuration: " + problems);
        }

        return config;
    }

}
