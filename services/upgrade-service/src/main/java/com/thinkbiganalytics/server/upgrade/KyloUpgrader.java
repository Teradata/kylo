/**
 *
 */
package com.thinkbiganalytics.server.upgrade;

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

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.KyloVersionUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Arrays;
import java.util.Properties;

/**
 * Performs all upgrade steps within a metadata transaction.
 */
public class KyloUpgrader {

    private static final Logger log = LoggerFactory.getLogger(KyloUpgrader.class);

    public static final String KYLO_UPGRADE = "kyloUpgrade";


    public void upgrade() {
        System.setProperty(SpringApplication.BANNER_LOCATION_PROPERTY, "upgrade-banner.txt");
        ConfigurableApplicationContext upgradeCxt = new SpringApplicationBuilder(KyloUpgradeConfig.class)
            .web(true)
            .profiles(KYLO_UPGRADE)
            .run();
        try {
            KyloUpgradeService upgradeService = upgradeCxt.getBean(KyloUpgradeService.class);
            // Keep upgrading until upgrade() returns true, i.e. we are up-to-date;
            while (!upgradeService.upgradeNext()) {
                ;
            }
        } finally {
            upgradeCxt.close();
        }
    }

    public boolean isUpgradeRequired() {
        KyloVersion buildVer = KyloVersionUtil.getBuildVersion();
        KyloVersion currentVer = getCurrentVersion();

        return currentVer == null || !buildVer.matches(currentVer.getMajorVersion(),
                                                       currentVer.getMinorVersion(),
                                                       currentVer.getPointVersion());
    }

    /**
     * Return the database version for Kylo.
     *
     * @return the version of Kylo stored in the database
     */
    public KyloVersion getCurrentVersion() {
        String profiles = System.getProperty("spring.profiles.active", "");
        if (!profiles.contains("native")) {
            profiles = StringUtils.isEmpty(profiles) ? "native" : profiles + ",native";
            System.setProperty("spring.profiles.active", profiles);
        }
        //Spring is needed to load the Spring Cloud context so we can decrypt the passwords for the database connection
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("kylo-upgrade-check-application-context.xml");
        loadProfileProperties(ctx);
        ctx.refresh();
        KyloUpgradeDatabaseVersionChecker upgradeDatabaseVersionChecker = (KyloUpgradeDatabaseVersionChecker) ctx.getBean("kyloUpgradeDatabaseVersionChecker");
        KyloVersion kyloVersion = upgradeDatabaseVersionChecker.getDatabaseVersion();
        ctx.close();
        return kyloVersion;
    }

    private void loadProfileProperties(ClassPathXmlApplicationContext ctx) {
        Arrays.asList(ctx.getEnvironment().getActiveProfiles()).stream().filter(p -> !"native".equalsIgnoreCase(p)).forEach(p -> {
            try {
                Properties properties = new Properties();
                properties.load(this.getClass().getClassLoader().getResourceAsStream("application-" + p + ".properties"));

                PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource(p, properties);
                ctx.getEnvironment().getPropertySources().addLast(propertiesPropertySource);
            } catch (Exception e) {
                log.warn("Unable to load properties for profile " + p);
            }
        });
    }

}
