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

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.KyloVersionUtil;
import com.thinkbiganalytics.metadata.api.app.KyloVersionProvider;
import com.thinkbiganalytics.server.upgrade.KyloUpgradeDatabaseVersionChecker;

/**
 * Performs all upgrade steps within a metadata transaction.
 */
public class KyloUpgrader {
    
    private static final Logger log = LoggerFactory.getLogger(KyloUpgrader.class);
    
    
    @Inject
    private MetadataAccess metadata;
    
    @Inject
    private UpgradeKyloService upgradeService;

    @Inject
    private KyloVersionProvider kyloVersionProvider;
    
    public boolean upgrade() {
        
        return true;
    }
    
    public boolean isUpgradeRequired() {
        KyloVersion buildVersion = KyloVersionUtil.getBuildVersion();
        
        return false;
    }
    
    public KyloVersion getCurrentVersion() {
        
        return null;
    }
    
//    public boolean isUpToDate() {
//        return metadata.commit(() -> {
//            return 
//        }, MetadataAccess.SERVICE);
//    }
//
//    public boolean upgrade() {
//        return metadata.commit(() -> {
//            if(!kyloVersionProvider.isUpToDate()) {
//                KyloVersion version = upgradeService.getCurrentVersion();
//                return upgradeService.upgradeFrom(version);
//            }
//            return true;
//        }, MetadataAccess.SERVICE);
//    }

    /**
     * Return the database version for Kylo.
     *
     * @return the version of Kylo stored in the database
     */
    private KyloVersion getDatabaseVersion() {

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
