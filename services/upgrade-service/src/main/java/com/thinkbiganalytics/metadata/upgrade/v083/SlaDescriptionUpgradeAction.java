package com.thinkbiganalytics.metadata.upgrade.v083;

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
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.rest.model.schema.TableSetup;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementService;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.DerivedDatasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.sla.ServiceLevelAgreementDescriptionProvider;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Ensures the SLA_DESCRIPTION table is populated with the SLA data from Modeshape
 */
@Component("slaUpgradeAction083")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class SlaDescriptionUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(SlaDescriptionUpgradeAction.class);

    @Inject
    private ServiceLevelAgreementService serviceLevelAgreementService;

    @Inject
    private ServiceLevelAgreementDescriptionProvider serviceLevelAgreementDescriptionProvider;

    @Inject
    private ServiceLevelAgreementProvider serviceLevelAgreementProvider;

    @Inject
    private FeedProvider feedProvider;

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.8", "3", "");
    }

    @Override
    public void upgradeTo(final KyloVersion startingVersion) {
        log.info("Adding SLA Descriptions.  Starting version: {}", startingVersion);
       serviceLevelAgreementService.getServiceLevelAgreements().stream().filter(sla -> !sla.getFeeds().isEmpty())
           .forEach(feedSla -> {
               Set<Feed.ID> feedIds = feedSla.getFeeds().stream().map(feed -> feedProvider.resolveId(feed.getId())).collect(Collectors.toSet());
               serviceLevelAgreementDescriptionProvider.updateServiceLevelAgreement(serviceLevelAgreementProvider.resolve(feedSla.getId()), feedSla.getName(),feedSla.getDescription(),feedIds,null);
            });
    }
}
