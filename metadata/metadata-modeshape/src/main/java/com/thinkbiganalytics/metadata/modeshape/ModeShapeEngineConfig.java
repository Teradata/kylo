/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeAuthConfig;

import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.JcrRepository;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.jcr.api.txn.TransactionManagerLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import javax.inject.Inject;
import javax.jcr.Repository;

/**
 *
 */
@Configuration
@Import({MetadataJcrConfig.class, ModeShapeAuthConfig.class})
public class ModeShapeEngineConfig {

    private static final Logger log = LoggerFactory.getLogger(ModeShapeEngineConfig.class);

    public static final String[] CONFIG_PROPS = {"modeshape.datasource.driverClassName",
                                                  "modeshape.datasource.url",
                                                  "modeshape.datasource.username",
                                                  "modeshape.datasource.password"
    };

    @Inject
    private Environment environment;


    @Bean
    public TransactionManagerLookup transactionManagerLookup() throws IOException {
        return metadataRepoConfig().getTransactionManagerLookup();
    }

    @Bean
    public RepositoryConfiguration metadataRepoConfig() throws IOException {
        // Expose the values of the config properties as system properties so that they can be used
        // for variable substitution in the ModeShape json config.
        for (String prop : CONFIG_PROPS) {
            if (this.environment.containsProperty(prop)) {
                System.setProperty(prop, this.environment.getProperty(prop));
            }
        }

        ClassPathResource res = new ClassPathResource("/metadata-repository.json");
        RepositoryConfiguration config = RepositoryConfiguration.read(res.getURL());

        Problems problems = config.validate();
        if (problems.hasErrors()) {
            log.error("Problems with the ModeShape repository configuration: \n{}", problems);
            throw new RuntimeException("Problems with the ModeShape repository configuration: " + problems);
        }

//        config.getSecurity();

        return config;
    }

    @Bean(destroyMethod="shutdown")
    public ModeShapeEngine modeShapeEngine() {
        ModeShapeEngine engine = new ModeShapeEngine();
        log.info("Starting ModeShape engine...");
        engine.start();
        log.info("ModeShape engine started");
        return engine;
    }

    @Bean(name = "metadataJcrRepository")
    public Repository metadataJcrRepository() throws Exception {
        JcrRepository repo = modeShapeEngine().deploy(metadataRepoConfig());

        Problems problems = repo.getStartupProblems();
        if (problems.hasErrors()) {
            log.error("Problems starting the metadata ModeShape repository: {}  \n{}", repo.getName(), problems);
            throw new RuntimeException("Problems starting the ModeShape metadata repository: " + problems);
        }

        return repo;
    }
}
