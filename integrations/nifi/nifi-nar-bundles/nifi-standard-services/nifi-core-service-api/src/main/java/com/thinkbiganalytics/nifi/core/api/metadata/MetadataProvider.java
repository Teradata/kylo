package com.thinkbiganalytics.nifi.core.api.metadata;

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
