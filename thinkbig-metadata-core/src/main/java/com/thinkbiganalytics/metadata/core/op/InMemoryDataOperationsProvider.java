/**
 * 
 */
package com.thinkbiganalytics.metadata.core.op;

import java.nio.file.Path;
import java.util.List;

import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.event.ChangeEventListener;
import com.thinkbiganalytics.metadata.api.feed.Feed.ID;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangeSetCriteria;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperation.Result;
import com.thinkbiganalytics.metadata.api.op.DataOperationCriteria;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;

/**
 *
 * @author Sean Felten
 */
public class InMemoryDataOperationsProvider implements DataOperationsProvider {

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#beginOperation(com.thinkbiganalytics.metadata.api.feed.Feed.ID, com.thinkbiganalytics.metadata.api.dataset.Dataset.ID)
     */
    public DataOperation beginOperation(ID feedId, com.thinkbiganalytics.metadata.api.dataset.Dataset.ID dsId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#updateOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID, java.lang.String, com.thinkbiganalytics.metadata.api.op.DataOperation.Result)
     */
    public DataOperation updateOperation(
            com.thinkbiganalytics.metadata.api.op.DataOperation.ID id, String status, Result result) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#updateOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID, java.lang.String, java.lang.Exception)
     */
    public DataOperation updateOperation(
            com.thinkbiganalytics.metadata.api.op.DataOperation.ID id, String status, Exception ex) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#updateOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID, java.lang.String, com.thinkbiganalytics.metadata.api.op.ChangeSet)
     */
    public DataOperation updateOperation(
            com.thinkbiganalytics.metadata.api.op.DataOperation.ID id, String status, ChangeSet<?, ?> changes) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#createChangeSet(com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset, java.util.List)
     */
    public ChangeSet<DirectoryDataset, FileList> createChangeSet(DirectoryDataset ds, List<Path> paths) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#createChangeSet(com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset, int)
     */
    public ChangeSet<HiveTableDataset, HiveTableUpdate> createChangeSet(HiveTableDataset ds, int count) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#dataOperationCriteria()
     */
    public DataOperationCriteria dataOperationCriteria() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDataOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID)
     */
    public DataOperation getDataOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDataOperations()
     */
    public List<DataOperation> getDataOperations() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDataOperations(com.thinkbiganalytics.metadata.api.op.DataOperationCriteria)
     */
    public List<DataOperation> getDataOperations(DataOperationCriteria criteria) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#changeSetCriteria()
     */
    public ChangeSetCriteria changeSetCriteria() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getChangeSets(com.thinkbiganalytics.metadata.api.dataset.Dataset.ID)
     */
    public List<ChangeSet<?, ?>> getChangeSets(com.thinkbiganalytics.metadata.api.dataset.Dataset.ID dsId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getChangeSets(com.thinkbiganalytics.metadata.api.dataset.Dataset.ID, com.thinkbiganalytics.metadata.api.op.ChangeSetCriteria)
     */
    public List<ChangeSet<?, ?>> getChangeSets(
            com.thinkbiganalytics.metadata.api.dataset.Dataset.ID dsId, ChangeSetCriteria criteria) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#addListener(com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset, com.thinkbiganalytics.metadata.api.event.ChangeEventListener)
     */
    public void addListener(DirectoryDataset ds, ChangeEventListener<DirectoryDataset, FileList> listener) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#addListener(com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset, com.thinkbiganalytics.metadata.api.event.ChangeEventListener)
     */
    public void addListener(HiveTableDataset ds, ChangeEventListener<HiveTableDataset, HiveTableUpdate> listener) {
        // TODO Auto-generated method stub

    }

}
