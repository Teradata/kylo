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
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.DerivedDatasource;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;

import javax.inject.Inject;

/**
 * Ensures that Hive column metadata is included with Hive derived datasources for indexing.
 *
 * <p>Indexing of the schema for the destination Hive table of Data Ingest feeds has been moved from the Index Schema Service feed to an internal Kylo service. This allows the index to include Kylo
 * metadata such as column tags.</p>
 *
 * <p>The Index Schema Service only indexed Hive tables that matched the category system name and feed system name. Likewise, this action only updates Hive derived datasources that match the category
 * system name and feed system name.</p>
 */
@Component("hiveColumnsUpgradeAction083")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class HiveColumnsUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(HiveColumnsUpgradeAction.class);

    /**
     * Provides access to datasources
     */
    @Inject
    private DatasourceProvider datasourceProvider;

    /**
     * Provides access to feeds
     */
    @Inject
    private FeedManagerFeedService feedService;

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.8", "3", "");
    }

    @Override
    public void upgradeTo(final KyloVersion startingVersion) {
        log.info("Upgrading hive columns from version: {}", startingVersion);

        feedService.getFeeds().stream()
            .filter(feed -> Optional.ofNullable(feed.getTable()).map(TableSetup::getTableSchema).map(TableSchema::getFields).isPresent())
            .forEach(feed -> {
                final TableSchema schema = feed.getTable().getTableSchema();
                final DerivedDatasource datasource = datasourceProvider.findDerivedDatasource("HiveDatasource",
                                                                                              feed.getSystemCategoryName() + "." + feed.getSystemFeedName());
                if (datasource != null) {
                    log.info("Upgrading schema: {}/{}", schema.getDatabaseName(), schema.getSchemaName());
                    datasource.setGenericProperties(Collections.singletonMap("columns", (Serializable) schema.getFields()));
                }
            });
    }
}
