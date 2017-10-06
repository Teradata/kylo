package com.thinkbiganalytics.metadata.upgrade.v084;

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
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeException;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.modeshape.jcr.api.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.jcr.RepositoryException;

/**
 * Initializes all of the ModeShape indexes introduced in 0.8.4.
 */
@Component("reindexUpgradeAction084")
@Order(0)
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class ReindexUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(ReindexUpgradeAction.class);

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.8", "4", "");
    }

    @Override
    public void upgradeTo(final KyloVersion startingVersion) {
        log.info("Re-indexing metadata for version:: {}", startingVersion);

        try {
            Workspace workspace = (Workspace) JcrMetadataAccess.getActiveSession().getWorkspace();
            workspace.reindex();
        } catch (RepositoryException e) {
            log.error("Failed to re-index metadata", e);
            throw new UpgradeException("Failed to re-index metadata", e);
        }
    }
}
