/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import org.infinispan.schematic.document.ParsingException;
import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.ConfigurationException;
import org.modeshape.jcr.JcrRepository;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.RepositoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class ModeShapeEngineConfig {
    
    private static final Logger log = LoggerFactory.getLogger(ModeShapeEngineConfig.class);
    
    @PostConstruct
    public void startEngine() throws ConfigurationException, ParsingException, FileNotFoundException, RepositoryException {
        log.info("Starting ModeShap engine...");
        modeShapeEngine().start();
        log.info("ModeShap engine started");
    }
    
    @PreDestroy
    public void stopEngine() throws InterruptedException, ExecutionException {
        log.info("Stopping ModeShap engine...");
        Future<Boolean> future = modeShapeEngine().shutdown();
        if ( future.get() ) {
            log.info("ModeShap engine stopped");
        }
    }
    
    @Bean
    public RepositoryConfiguration metadataRepoConfig() throws ParsingException, FileNotFoundException {
        // org.modeshape.jcr.URL = file:path/to/metadata-repository.json
//        ClassPathResource res = new ClassPathResource("/metadata-repository.json")
        RepositoryConfiguration config = RepositoryConfiguration.read("/metadata-repository.json");
        
        Problems problems = config.validate();
        if (problems.hasErrors()) {
            log.error("Problems with the ModeShape repository configuration: \n{}", problems);
            throw new RuntimeException("Problems with the ModeShape repository configuration: " + problems);
        }
        
        return config;
    }

    @Bean
    public ModeShapeEngine modeShapeEngine() {
        return new ModeShapeEngine();
    }
    
    @Bean
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
