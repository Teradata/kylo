/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import java.nio.file.Path;
import java.util.List;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.event.ChangeEventListener;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.op.DataOperation.Result;

/**
 *
 * @author Sean Felten
 */
public interface DataOperationsProvider {

    DataOperation beginOperation(Feed.ID feedId, Dataset.ID dsId);
    DataOperation updateOperation(DataOperation.ID id, String status, Result result);
    DataOperation updateOperation(DataOperation.ID id, String status, Exception ex);
    DataOperation updateOperation(DataOperation.ID id, String status, ChangeSet<?, ?> changes);
    
    ChangeSet<DirectoryDataset, FileList> createChangeSet(DirectoryDataset ds, List<Path> paths);
    ChangeSet<HiveTableDataset, HiveTableUpdate> createChangeSet(HiveTableDataset ds, int count);
    
    DataOperationCriteria dataOperationCriteria();

    DataOperation getDataOperation(DataOperation.ID id);
    List<DataOperation> getDataOperations();
    List<DataOperation> getDataOperations(DataOperationCriteria criteria);
    
    ChangeSetCriteria changeSetCriteria();
    
    List<ChangeSet<?, ?>> getChangeSets(Dataset.ID dsId);
    List<ChangeSet<?, ?>> getChangeSets(Dataset.ID dsId, ChangeSetCriteria criteria);

    void addListener(DirectoryDataset ds, ChangeEventListener<DirectoryDataset, FileList> listener);
    void addListener(HiveTableDataset ds, ChangeEventListener<HiveTableDataset, HiveTableUpdate> listener);
}
