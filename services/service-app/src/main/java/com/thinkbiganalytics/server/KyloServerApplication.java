package com.thinkbiganalytics.server;

/*-
 * #%L
 * thinkbig-service-app
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

import com.google.common.collect.Sets;
import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.KyloVersionUtil;
import com.thinkbiganalytics.metadata.config.OperationalMetadataConfig;
import com.thinkbiganalytics.rest.SpringJerseyConfiguration;
import com.thinkbiganalytics.rest.exception.profiles.KyloProfileException;
import com.thinkbiganalytics.security.core.SecurityCoreConfig;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


@Configuration
@SpringBootApplication
@EnableConfigurationProperties
@Import({DatabaseConfiguration.class, OperationalMetadataConfig.class, SpringJerseyConfiguration.class, SecurityCoreConfig.class})
@ComponentScan("com.thinkbiganalytics")
public class KyloServerApplication implements SchedulingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(KyloServerApplication.class);

    public static void main(String[] args) {
        // Configure java.util.logging for Kylo Spark Shell
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        // Run upgrader
        KyloUpgrader upgrader = new KyloUpgrader();

        if (upgrader.isUpgradeRequired()) {
            try {
                KyloVersion currentVersion = upgrader.getCurrentVersion();
                log.info("*****  Upgrade required - this may take some time  *****");
                log.info("Beginning upgrade from version {} ...", currentVersion == null ? "unknown" : currentVersion);
                upgrader.upgrade();
                log.info("*****  Upgrading complete  *****");
            } catch (Exception e) {
                log.error("Error during upgrade: {}", e.getMessage());
                log.error("*****  Upgrading failed  *****");
                System.exit(1);
            }
        } else {
            log.info("Kylo v{} is up to date.  Starting the application.", KyloVersionUtil.getBuildVersion());
        }

        // Run services
        System.setProperty(SpringApplication.BANNER_LOCATION_PROPERTY, "banner.txt");
        SpringApplication application = new SpringApplication("classpath:application-context.xml");
        application.addInitializers(ctx -> validateMinProfiles(ctx));
        application.run(args);
    }

    @Bean(destroyMethod = "shutdown")
    public Executor scheduledTaskExecutor() {
        return Executors.newScheduledThreadPool(25);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setScheduler(scheduledTaskExecutor());
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            if (log.isTraceEnabled()) {
                String[] beanNames = ctx.getBeanDefinitionNames();
                Arrays.sort(beanNames);
                for (String beanName : beanNames) {
                    log.trace("Bean name='{}', type='{}'", beanName);
                }
            }
        };
    }

    private static void validateMinProfiles(ConfigurableApplicationContext ctx) {
        Set<String> profiles = Sets.newHashSet(ctx.getEnvironment().getActiveProfiles());

        Set<String> minProfiles = Sets.newHashSet("native", "jms-activemq");
        if (!profiles.containsAll(minProfiles)) {
            throw new KyloProfileException("Missing one or more of the following profiles on start up: " + minProfiles.toString());
        }

        if (profiles.contains("kylo-shell")) {
            if (!profiles.contains("auth-spark")) {
                throw new KyloProfileException("kylo-shell defined and auth-spark profile is missing");
            }
        } else {
            if (!profiles.contains("kylo-livy")) {
                // TODO: change error message to describe that it must be kylo-shell or kylo-livy once Livy is officially supported
                throw new KyloProfileException("kylo-shell profile must be defined");
            }
        }

        Set<String> searchProfiles = Sets.newHashSet("search-es", "search-esr", "search-solr");
        if (Sets.intersection(profiles, searchProfiles).size() > 1) {
            throw new KyloProfileException("Cannot specify more than one search profile on startup");
        }

        Set<String> nifiProfiles = Sets.newHashSet("nifi-v1", "nifi-v1.1", "nifi-v1.2", "nifi-v1.3", "nifi-v1.4", "nifi-v1.5");
        if (Sets.intersection(profiles, nifiProfiles).size() != 1) {
            throw new KyloProfileException("Must specify one and only one valid NiFi profile");
        }
    }

}
