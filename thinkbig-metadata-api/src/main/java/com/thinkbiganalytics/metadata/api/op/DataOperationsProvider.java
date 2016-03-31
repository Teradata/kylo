/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.datasource.filesys.FileList;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableUpdate;
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
    DataOperation beginOperation(Feed.ID feedId, Datasource.ID dsId, DateTime start);
    DataOperation updateOperation(DataOperation.ID id, String status, State result);
    DataOperation updateOperation(DataOperation.ID id, String status, Exception ex);
    <D extends Datasource, C extends ChangeSet> DataOperation updateOperation(DataOperation.ID id, String status, Dataset<D, C> changes);
//    DataOperation updateOperation(DataOperation.ID id, String status, Dataset<Datasource, ChangeSet> changes);
    
    Dataset<DirectoryDataset, FileList> createChangeSet(DirectoryDataset ds, List<Path> paths);
    Dataset<HiveTableDataset, HiveTableUpdate> createChangeSet(HiveTableDataset ds, int count);
    
    DataOperationCriteria dataOperationCriteria();

    DataOperation getDataOperation(DataOperation.ID id);
    List<DataOperation> getDataOperations();
    List<DataOperation> getDataOperations(DataOperationCriteria criteria);
    
    DatasetCriteria changeSetCriteria();
    
    <D extends Datasource, C extends ChangeSet> Collection<Dataset<D, C>> getChangeSets(Datasource.ID dsId);
    <D extends Datasource, C extends ChangeSet> Collection<Dataset<D, C>> getChangeSets(Datasource.ID dsId, DatasetCriteria criteria);

    void addListener(DataChangeEventListener<Datasource, ChangeSet> listener);
    void addListener(DirectoryDataset ds, DataChangeEventListener<DirectoryDataset, FileList> listener);
    void addListener(HiveTableDataset ds, DataChangeEventListener<HiveTableDataset, HiveTableUpdate> listener);
}
