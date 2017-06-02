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
import com.thinkbiganalytics.metadata.upgrade.KyloUpgrader;
import com.thinkbiganalytics.metadata.upgrade.UpgradeKyloConfig;
import com.thinkbiganalytics.rest.SpringJerseyConfiguration;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


@Configuration
@SpringBootApplication
@EnableAutoConfiguration(exclude = {VelocityAutoConfiguration.class})
@EnableConfigurationProperties
@Import({DatabaseConfiguration.class, OperationalMetadataConfig.class, SpringJerseyConfiguration.class})
@ComponentScan("com.thinkbiganalytics")
public class KyloServerApplication implements SchedulingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(KyloServerApplication.class);

    public static void main(String[] args) {

        KyloVersion dbVersion = getDatabaseVersion();

        boolean skipUpgrade = KyloVersionUtil.isUpToDate(dbVersion);

        if (!skipUpgrade) {
            boolean upgradeComplete = false;
            do {
                log.info("Upgrading...");
                System.setProperty(SpringApplication.BANNER_LOCATION_PROPERTY, "upgrade-banner.txt");
                ConfigurableApplicationContext cxt = SpringApplication.run(UpgradeKyloConfig.class);
                KyloUpgrader upgrader = cxt.getBean(KyloUpgrader.class);
                upgradeComplete = upgrader.upgrade();
                cxt.close();
            } while (!upgradeComplete);
            log.info("Upgrading complete");
        }
        else {
            log.info("Kylo v{} is up to date.  Starting the application.",dbVersion);
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


    /**
     * Return the database version for Kylo.
     *
     * @return the version of Kylo stored in the database
     */
    private static KyloVersion getDatabaseVersion() {

        try {
            //ensure native profile is there for spring to load
            String profiles = System.getProperty("spring.profiles.active", "");
            if (!profiles.contains("native")) {
                profiles = StringUtils.isEmpty(profiles) ? "native" : profiles + ",native";
                System.setProperty("spring.profiles.active", profiles);
            }
            //Spring is needed to load the Spring Cloud context so we can decrypt the passwords for the database connection
            ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("kylo-upgrade-check-application-context.xml");
            ctx.refresh();
            KyloUpgradeDatabaseVersionChecker upgradeDatabaseVersionChecker = (KyloUpgradeDatabaseVersionChecker) ctx.getBean("kyloUpgradeDatabaseVersionChecker");
            KyloVersion kyloVersion = upgradeDatabaseVersionChecker.getDatabaseVersion();
            ctx.close();
            return kyloVersion;
        } catch (Exception e) {
            log.error("Failed get the database version prior to upgrade.  The Kylo Upgrade application will load by default. {}", e.getMessage());
        }
        return null;


    }
}
