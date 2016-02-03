/**
 * 
 */
package com.thinkbiganalytics.metadata.core.spi;

import java.nio.file.Path;
import java.util.List;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.DatasetCriteria;
import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperationCriteria;
import com.thinkbiganalytics.metadata.api.op.DataOperation.State;

/**
 *
 * @author Sean Felten
 */
public interface MetadataStorage {

//    DirectoryDataset createDirectoryDataset(String name, String descr, Path dir);
//    HiveTableDataset createHiveTableDataset(String name, String database, String table);
    
//    DataOperation beginOperation(Feed.ID feedId, Dataset.ID dsId);
//    DataOperation updateOperation(DataOperation.ID id, String status, State result);
//    DataOperation updateOperation(DataOperation.ID id, String status, Exception ex);
//    DataOperation updateOperation(DataOperation.ID id, String status, ChangeSet<?, ?> changes);
//    
//    ChangeSet<DirectoryDataset, FileList> createChangeSet(DirectoryDataset ds, List<Path> paths);
//    ChangeSet<HiveTableDataset, HiveTableUpdate> createChangeSet(HiveTableDataset ds, int count);
    
//    FeedSource createFeedSource(Dataset.ID id);
//    FeedDestination createFeedDestination(Dataset.ID id);
//    Feed createFeed(String name, String descr, Dataset.ID srcId, Dataset.ID destId);
//    Feed createFeed(String name, String descr, FeedSource src, FeedDestination dest);
//    
//    FeedCriteria feedCriteria();
//    DatasetCriteria datasetCriteria();
//    DataOperationCriteria dataOperationCriteria();
    
//    Feed getFeed(Feed.ID id);
//    List<Feed> getFeeds();
//    List<Feed> getFeeds(FeedCriteria criteria);
    
//    DataOperation getDataOperation(DataOperation.ID id);
//    List<DataOperation> getDataOperations();
//    List<DataOperation> getDataOperations(DataOperationCriteria criteria);
    
}
