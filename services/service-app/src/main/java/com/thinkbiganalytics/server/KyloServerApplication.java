package com.thinkbiganalytics.server;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

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

import com.thinkbiganalytics.metadata.config.OperationalMetadataConfig;
import com.thinkbiganalytics.metadata.upgrade.KyloUpgrader;
import com.thinkbiganalytics.metadata.upgrade.UpgradeKyloConfig;
import com.thinkbiganalytics.rest.SpringJerseyConfiguration;

@Configuration
@SpringBootApplication
@EnableAutoConfiguration(exclude = {VelocityAutoConfiguration.class})
@EnableConfigurationProperties
@EnableConfigServer
@Import({DatabaseConfiguration.class, OperationalMetadataConfig.class, SpringJerseyConfiguration.class})
@ComponentScan("com.thinkbiganalytics")
public class KyloServerApplication implements SchedulingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(KyloServerApplication.class);

    public static void main(String[] args) {
        boolean upgradeComplete = false;
        do {
            log.info("Upgrading...");
            ConfigurableApplicationContext cxt = SpringApplication.run(UpgradeKyloConfig.class);
            KyloUpgrader upgrader = cxt.getBean(KyloUpgrader.class);
            upgradeComplete = upgrader.upgrade();
            cxt.close();
        } while (! upgradeComplete);
        log.info("Upgrading complete");
        
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
