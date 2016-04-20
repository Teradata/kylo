/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.op;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource;
import com.thinkbiganalytics.metadata.api.datasource.filesys.FileList;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.event.DataChangeEventListener;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperation.ID;
import com.thinkbiganalytics.metadata.api.op.DataOperation.State;
import com.thinkbiganalytics.metadata.api.op.DataOperationCriteria;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.api.op.Dataset;

/**
 *
 * @author Sean Felten
 */
public class DataOperationsProviderImpl implements DataOperationsProvider {

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#asOperationId(java.lang.String)
     */
    @Override
    public ID asOperationId(String opIdStr) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#beginOperation(com.thinkbiganalytics.metadata.api.feed.FeedDestination, org.joda.time.DateTime)
     */
    @Override
    public DataOperation beginOperation(FeedDestination dest, DateTime start) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#beginOperation(com.thinkbiganalytics.metadata.api.feed.Feed.ID, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID, org.joda.time.DateTime)
     */
    @Override
    public DataOperation beginOperation(
            com.thinkbiganalytics.metadata.api.feed.Feed.ID feedId,
            com.thinkbiganalytics.metadata.api.datasource.Datasource.ID dsId,
            DateTime start) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#updateOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID, java.lang.String, com.thinkbiganalytics.metadata.api.op.DataOperation.State)
     */
    @Override
    public DataOperation updateOperation(ID id, String status, State result) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#updateOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID, java.lang.String, java.lang.Exception)
     */
    @Override
    public DataOperation updateOperation(ID id, String status, Exception ex) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#updateOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID, java.lang.String, com.thinkbiganalytics.metadata.api.op.Dataset)
     */
    @Override
    public <D extends Datasource, C extends ChangeSet> DataOperation updateOperation(
            ID id,
            String status,
            Dataset<D, C> changes) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#createDataset(com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource, java.util.List)
     */
    @Override
    public Dataset<DirectoryDatasource, FileList> createDataset(DirectoryDatasource ds, List<Path> paths) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#createDataset(com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource, int)
     */
    @Override
    public Dataset<HiveTableDatasource, HiveTableUpdate> createDataset(HiveTableDatasource ds, int count) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#dataOperationCriteria()
     */
    @Override
    public DataOperationCriteria dataOperationCriteria() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDataOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID)
     */
    @Override
    public DataOperation getDataOperation(ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDataOperations()
     */
    @Override
    public List<DataOperation> getDataOperations() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDataOperations(com.thinkbiganalytics.metadata.api.op.DataOperationCriteria)
     */
    @Override
    public List<DataOperation> getDataOperations(DataOperationCriteria criteria) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#DatasetCriteria()
     */
    @Override
    public com.thinkbiganalytics.metadata.api.op.DatasetCriteria DatasetCriteria() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDatasets(com.thinkbiganalytics.metadata.api.datasource.Datasource.ID)
     */
    @Override
    public <D extends Datasource, C extends ChangeSet> Collection<Dataset<D, C>> getDatasets(
            com.thinkbiganalytics.metadata.api.datasource.Datasource.ID dsId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDatasets(com.thinkbiganalytics.metadata.api.datasource.Datasource.ID, com.thinkbiganalytics.metadata.api.op.DatasetCriteria)
     */
    @Override
    public <D extends Datasource, C extends ChangeSet> Collection<Dataset<D, C>> getDatasets(
            com.thinkbiganalytics.metadata.api.datasource.Datasource.ID dsId,
            com.thinkbiganalytics.metadata.api.op.DatasetCriteria criteria) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#addListener(com.thinkbiganalytics.metadata.api.event.DataChangeEventListener)
     */
    @Override
    public void addListener(DataChangeEventListener<Datasource, ChangeSet> listener) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#addListener(com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource, com.thinkbiganalytics.metadata.api.event.DataChangeEventListener)
     */
    @Override
    public void addListener(DirectoryDatasource ds, DataChangeEventListener<DirectoryDatasource, FileList> listener) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#addListener(com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource, com.thinkbiganalytics.metadata.api.event.DataChangeEventListener)
     */
    @Override
    public void addListener(
            HiveTableDatasource ds,
            DataChangeEventListener<HiveTableDatasource, HiveTableUpdate> listener) {
        // TODO Auto-generated method stub

    }

}
