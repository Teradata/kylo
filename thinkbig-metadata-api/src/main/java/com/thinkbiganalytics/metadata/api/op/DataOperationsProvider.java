/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.event.DataChangeEventListener;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.op.DataOperation.ID;
import com.thinkbiganalytics.metadata.api.op.DataOperation.State;

/**
 *
 * @author Sean Felten
 */
public interface DataOperationsProvider {
    
    ID asOperationId(String opIdStr);

    DataOperation beginOperation(FeedDestination dest, DateTime start);
    DataOperation beginOperation(Feed.ID feedId, Dataset.ID dsId, DateTime start);
    DataOperation updateOperation(DataOperation.ID id, String status, State result);
    DataOperation updateOperation(DataOperation.ID id, String status, Exception ex);
    DataOperation updateOperation(DataOperation.ID id, String status, ChangeSet<?, ?> changes);
    
    ChangeSet<DirectoryDataset, FileList> createChangeSet(DirectoryDataset ds, List<Path> paths);
    ChangeSet<HiveTableDataset, HiveTableUpdate> createChangeSet(HiveTableDataset ds, int count);
    
    DataOperationCriteria dataOperationCriteria();

    DataOperation getDataOperation(DataOperation.ID id);
    Collection<DataOperation> getDataOperations();
    Collection<DataOperation> getDataOperations(DataOperationCriteria criteria);
    
    ChangeSetCriteria changeSetCriteria();
    
    Collection<ChangeSet<?, ?>> getChangeSets(Dataset.ID dsId);
    Collection<ChangeSet<?, ?>> getChangeSets(Dataset.ID dsId, ChangeSetCriteria criteria);

    void addListener(DirectoryDataset ds, DataChangeEventListener<DirectoryDataset, FileList> listener);
    void addListener(HiveTableDataset ds, DataChangeEventListener<HiveTableDataset, HiveTableUpdate> listener);
}
