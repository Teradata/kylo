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
import com.thinkbiganalytics.search.api.RepositoryIndexConfiguration;

import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.JcrRepository;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.jcr.api.txn.TransactionManagerLookup;
import org.modeshape.schematic.document.EditableDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.jcr.Repository;

/**
 *
 */
@Configuration
@Import({MetadataJcrConfig.class, ModeShapeAuthConfig.class})
public class ModeShapeEngineConfig {

    private static final Logger log = LoggerFactory.getLogger(ModeShapeEngineConfig.class);
    
    public static final String INDEX_DIR_PROP = "modeshape.index.dir";
    public static final File DEFAULT_INDEX_DIR = new File("/opt/kylo/modeshape/modeshape-local-index");

    private static final String[] CONFIG_PROPS = {"modeshape.datasource.driverClassName",
                                                  "modeshape.datasource.url",
                                                  "modeshape.datasource.username",
                                                  "modeshape.datasource.password",
                                                  INDEX_DIR_PROP
    };

    @Inject
    private Environment environment;

    @Inject
    private Optional<RepositoryIndexConfiguration> repositoryIndexConfiguration;

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
        
        File dir = DEFAULT_INDEX_DIR;
        if (this.environment.containsProperty(INDEX_DIR_PROP)) {
            String idxPath = this.environment.getProperty(INDEX_DIR_PROP);
            dir = new File(idxPath);
        }
        
        try {
            // Create index directory if necessary.
            dir.mkdirs();
        } catch (Exception e) {
            log.error("Failed to verify Modeshape index directory in property: {}", INDEX_DIR_PROP, e);
            throw new IllegalStateException("Failed to verify Modeshape index directory in property: " + INDEX_DIR_PROP, e);
        }

        ClassPathResource res = new ClassPathResource("/metadata-repository.json");
        final AtomicReference<RepositoryConfiguration> config = new AtomicReference<>(RepositoryConfiguration.read(res.getURL()));
        
        repositoryIndexConfiguration.ifPresent(idxConfig -> {
            RepositoryConfiguration updatedConfigWithIndexes = idxConfig.build();
            EditableDocument original = config.get().edit();
            EditableDocument added = updatedConfigWithIndexes.edit();
            original.merge(added);
            RepositoryConfiguration updatedConfig = new RepositoryConfiguration(original, config.get().getName());
            log.debug("Original ModeShape configuration: {}", config.toString());
            log.debug("ModeShape indexing configuration: {}", updatedConfigWithIndexes.toString());
            log.debug("Updated ModeShape configuration: {}", updatedConfig.toString());
            config.set(updatedConfig);
            log.info("ModeShape indexing configured");
        });

        Problems problems = config.get().validate();
        if (problems.hasErrors()) {
            log.error("Problems with the ModeShape repository configuration: \n{}", problems);
            throw new RuntimeException("Problems with the ModeShape repository configuration: " + problems);
        }

//        config.getSecurity();

        return config.get();
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

        try {
            Problems problems = repo.getStartupProblems();
            if (problems.hasErrors()) {
                log.error("Problems starting the metadata ModeShape repository: {}  \n{}", repo.getName(), problems);
                throw new RuntimeException("Problems starting the ModeShape metadata repository: " + problems);
            }
        } catch (NullPointerException e) {
            // This gets thrown sometimes when attempting to retrieve the startup problems.  It looks 
            // like it is probably a ModeShape bug that can happen even when the repo starts successfully.
            // Just log a warning about it and proceed as if everything is fine.
            log.warn("Retrieved a NullPointerException when attempting to check for startup errors - this is likely a ModeShape bug and can usually be ignored");
        }

        return repo;
    }
}
