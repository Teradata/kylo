package com.thinkbiganalytics.nifi.core.api.metadata;

/*-
 * #%L
 * thinkbig-nifi-core-service-api
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

import com.thinkbiganalytics.metadata.api.op.FeedDependencyDeltaResults;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation.State;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset;
import com.thinkbiganalytics.metadata.rest.model.op.HiveTablePartitions;
import com.thinkbiganalytics.metadata.sla.api.Metric;

import org.joda.time.DateTime;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * @author Sean Felten
 */
public interface MetadataProvider {
    
    String getFeedId(String category, String feedName);

    FeedDependencyDeltaResults getFeedDependentResultDeltas(String feedId);

    Feed ensureFeed(String categoryName, String feedName, String string);

    Datasource getDatasourceByName(String dsName);

    Feed ensureFeedSource(String feedId, String datasourceId);

    Feed ensureFeedDestination(String feedId, String id);

    Feed ensurePrecondition(String feedId, Metric... metrics);
    
    Properties updateFeedProperties(String feedId, Properties props);

    DirectoryDatasource ensureDirectoryDatasource(String datasetName, String string, Path path);

    HiveTableDatasource ensureHiveTableDatasource(String datasetName, String string, String databaseName, String tableName);

    Dataset createDataset(DirectoryDatasource dds, Path... paths);

    Dataset createDataset(DirectoryDatasource dds, ArrayList<Path> paths);

    Dataset createDataset(HiveTableDatasource hds, HiveTablePartitions partitions);

    DataOperation beginOperation(FeedDestination feedDestination, DateTime opStart);

    DataOperation completeOperation(String id, String string, Dataset changeSet);

    DataOperation completeOperation(String id, String string, State state);

    /**
     * Gets the properties of the specified feed.
     *
     * @param id the feed id
     * @return the properties
     */
    Properties getFeedProperties(@Nonnull final String id);

    /**
     * Merges the specified properties into the feed's properties.
     *
     * @param id the feed id
     * @param props the new properties
     * @return the merged properties
     */
    Properties mergeFeedProperties(@Nonnull final String id, @Nonnull final Properties props);
}
