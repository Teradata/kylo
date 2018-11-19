package com.thinkbiganalytics.spark;

/*-
 * #%L
 * thinkbig-spark-shell-client-app
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

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.thinkbiganalytics.security.core.SecurityCoreConfig;
import com.thinkbiganalytics.spark.service.IdleMonitorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.websocket.WebSocketAutoConfiguration;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Instantiates a REST server for executing Spark scripts.
 */
@Profile("kylo-shell")
// ignore auto-configuration classes outside Spark Shell
@ComponentScan(basePackages = {"com.thinkbiganalytics.spark", "com.thinkbiganalytics.kylo.catalog.spark"},
               excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = SecurityCoreConfig.class))
@PropertySource(value = {"classpath:sparkDefaults.properties", "classpath:spark.properties", "classpath:sparkDevOverride.properties"}, ignoreResourceNotFound = true)
@SpringBootApplication(exclude = {WebSocketAutoConfiguration.class})
public class SparkShellApp {

    private static final Logger logger = LoggerFactory.getLogger(SparkShellApp.class);

    /**
     * Instantiates the REST server with the specified arguments.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        logger.info("Starting Kylo Spark Shell");

        final SpringApplication app = new SpringApplication(SparkShellApp.class);
        app.setAdditionalProfiles("kylo-shell");

        final Map<String, Object> properties = new HashMap<>();
        properties.put("kylo.client.id", System.getenv("KYLO_CLIENT_ID"));
        properties.put("kylo.client.secret", System.getenv("KYLO_CLIENT_SECRET"));
        app.setDefaultProperties(properties);

        // Ignore application listeners that will load kylo-services configuration
        final List<ApplicationListener<?>> listeners = FluentIterable.from(app.getListeners())
            .filter(Predicates.not(
                Predicates.or(
                    Predicates.instanceOf(ConfigFileApplicationListener.class),
                    Predicates.instanceOf(LoggingApplicationListener.class)
                )
            ))
            .toList();
        app.setListeners(listeners);

        // Start app
        final ApplicationContext context = app.run(args);

        if (logger.isInfoEnabled()) {
            logger.info("SparkLauncher Active Profiles = '{}'", Arrays.toString(context.getEnvironment().getActiveProfiles()));
        }

        // Keep main thread running until the idle timeout
        context.getBean(IdleMonitorService.class).awaitIdleTimeout();
    }

}
