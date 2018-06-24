package com.thinkbiganalytics.metadata.upgrade.v092;

/*-
 * #%L
 * kylo-upgrade-service
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
import com.thinkbiganalytics.search.api.Search;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Deletes all indexed feed metadata, so that it can be re-indexed with updated IDs
 */
@Component("indexFeedMetadataUpgradeAction092")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class IndexFeedMetadataUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(IndexFeedMetadataUpgradeAction.class);

    @Inject
    private Search searchClient;

    @Autowired
    private Environment environment;

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.9", "2", "");
    }

    @Override
    public void upgradeTo(KyloVersion startingVersion) {
        log.info("****************** Start: Index Feed Metadata Upgrade Action ****************");

        String[] activeProfiles = this.environment.getActiveProfiles();
        for (String activeProfile : activeProfiles) {
            if (activeProfile.equals("search-esr")) {
                int deletedDocumentCountIndexDefault = searchClient.deleteAll("kylo-feeds-default", "default");
                log.info("Total feed metadata documents deleted (index: default): {}", deletedDocumentCountIndexDefault);
                int deletedDocumentCountIndexMetadata = searchClient.deleteAll("kylo-feeds-metadata", "default");
                log.info("Total feed metadata documents deleted (index: metadata): {}", deletedDocumentCountIndexMetadata);
            }
        }
        log.info("****************** End: Index Feed Metadata Upgrade Action ****************");
    }
}
