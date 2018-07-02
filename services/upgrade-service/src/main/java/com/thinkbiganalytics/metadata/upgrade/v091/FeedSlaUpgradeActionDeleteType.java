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
import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.inject.Inject;


/**
 * Converts the feed SLA relationships from extensible entities to nodes attached to the SLA nodes.
 * It then deletes the original relationship entities and deletes the relationship entity type.
 */
@Component("FeedSlaUpgradeActionDeleteType091")
@Order(999)
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class FeedSlaUpgradeActionDeleteType implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(FeedSlaUpgradeActionDeleteType.class);

    private static final String SLA_REL_TYPE_NAME = "feedSla";


    @Inject
    private ExtensibleTypeProvider typeProvider;


    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.9", "1", "");
    }

    @Override
    public void upgradeTo(final KyloVersion targetVersion) {
        log.info("Deleting tba:{} node type : {}",SLA_REL_TYPE_NAME, targetVersion);

        this.typeProvider.getTypes().forEach(type -> {
            String typeName = type.getName();

            if (typeName.equals(SLA_REL_TYPE_NAME)) {
                this.typeProvider.deleteType(type.getId());
            }
        });
    }

}