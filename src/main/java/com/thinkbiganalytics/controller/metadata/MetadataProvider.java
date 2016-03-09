/**
 * 
 */
package com.thinkbiganalytics.controller.metadata;

import java.nio.file.Path;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset;
import com.thinkbiganalytics.metadata.rest.model.sla.Metric;

/**
 *
 * @author Sean Felten
 */
public interface MetadataProvider {

    Feed ensureFeed(String feedName, String string);

    Datasource getDatasourceByName(String dsName);

    Feed ensureFeedSource(String feedId, String datasourceId);

    Feed ensureFeedDestination(String feedId, String id);
    
    Feed ensurePrecondition(String feedId, Metric... metrics);

    DataOperation beginOperation(FeedDestination feedDestination, DateTime opStart);

    Datasource ensureDirectoryDatasource(String datasetName, String string, Path path);

    Dataset createDataset(DirectoryDatasource dds, ArrayList<Path> paths);

    Datasource ensureHiveTableDatasource(String datasetName, String string, String databaseName, String tableName);

    Dataset createChangeSet(HiveTableDatasource hds, int i);

    DataOperation completeOperation(String id, String string, Dataset changeSet);

}
