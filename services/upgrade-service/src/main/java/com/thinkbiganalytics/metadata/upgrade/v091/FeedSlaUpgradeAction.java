package com.thinkbiganalytics.metadata.upgrade.v091;

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

import javax.inject.Inject;
import javax.jcr.Node;

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrFeedServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrServiceLevelAgreement;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Converts the feed SLA relationships from extensible entities to nodes attached to the SLA nodes.
 * It then deletes the original relationship entities and deletes the relationship entity type.
 */
@Component("FeedSlaUpgradeAction091")
@Order(998)
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class FeedSlaUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(FeedSlaUpgradeAction.class);
    
    private static final String SLA_REL_TYPE_NAME = "feedSla";

    
    @Inject
    private ExtensibleTypeProvider typeProvider;
    
    @Inject
    private ExtensibleEntityProvider entityProvider;
    
    @Inject
    private JcrFeedServiceLevelAgreementProvider feedSlaProvider;
    
    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.9", "1", "");
    }

    @Override
    public void upgradeTo(final KyloVersion targetVersion) {
        log.info("Refactoring feed SLA relationship management: {}", targetVersion);
        
        this.typeProvider.getTypes().forEach(type -> {
            String typeName = type.getName();
            
            if (typeName.equals(SLA_REL_TYPE_NAME)) {
                upgradeFeedSla(type);
            }
        });
    }

    private void upgradeFeedSla(ExtensibleType type) {
        List<ExtensibleEntity> slaRels = entityProvider.getEntities(SLA_REL_TYPE_NAME);
        
        for (ExtensibleEntity rel : slaRels) {
            ServiceLevelAgreement sla = JcrUtil.getJcrObject(rel.getProperty("sla"), JcrServiceLevelAgreement.class);
            Set<Feed> feeds = rel.getPropertyAsSet("feeds", Node.class).stream()
                            .map(JcrFeed::new)
                            .collect(Collectors.toSet());
            
            feedSlaProvider.relateFeeds(sla, feeds);
            entityProvider.deleteEntity(rel.getId());
        }
    }
}
