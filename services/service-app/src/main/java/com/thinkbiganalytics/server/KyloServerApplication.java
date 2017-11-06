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

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.KyloVersionUtil;
import com.thinkbiganalytics.metadata.config.OperationalMetadataConfig;
import com.thinkbiganalytics.rest.SpringJerseyConfiguration;
import com.thinkbiganalytics.security.core.SecurityCoreConfig;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


@Configuration
@SpringBootApplication
@EnableAutoConfiguration(exclude = {VelocityAutoConfiguration.class})
@EnableConfigurationProperties
@Import({DatabaseConfiguration.class, OperationalMetadataConfig.class, SpringJerseyConfiguration.class, SecurityCoreConfig.class})
@ComponentScan("com.thinkbiganalytics")
public class KyloServerApplication implements SchedulingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(KyloServerApplication.class);

    public static void main(String[] args) {
        SLF4JBridgeHandler.install();
        KyloUpgrader upgrader = new KyloUpgrader();

        if (upgrader.isUpgradeRequired()) {
            KyloVersion currentVersion = upgrader.getCurrentVersion();
            log.info("*****  Upgrade required - this may take some time  *****");
            log.info("Beginning upgrade from version {} ...", currentVersion == null ? "unknown" : currentVersion);
            upgrader.upgrade();
            log.info("*****  Upgrading complete  *****");
        }
        else {
            log.info("Kylo v{} is up to date.  Starting the application.",KyloVersionUtil.getBuildVersion());
        }

        System.setProperty(SpringApplication.BANNER_LOCATION_PROPERTY, "banner.txt");
        SpringApplication.run("classpath:application-context.xml", args);

    }

    @Bean(destroyMethod = "shutdown")
    public Executor scheduledTaskExecutor() {
        return Executors.newScheduledThreadPool(25);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setScheduler(scheduledTaskExecutor());
    }

}
