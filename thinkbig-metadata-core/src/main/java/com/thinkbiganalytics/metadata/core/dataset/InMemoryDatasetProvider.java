/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset;

import java.nio.file.Path;
import java.util.List;

import javax.inject.Inject;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.Dataset.ID;
import com.thinkbiganalytics.metadata.api.dataset.DatasetCriteria;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.event.ChangeEventListener;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;

import reactor.bus.EventBus;

/**
 *
 * @author Sean Felten
 */
public class InMemoryDatasetProvider implements DatasetProvider {

    public DatasetCriteria datasetCriteria() {
        // TODO Auto-generated method stub
        return null;
    }

    public DirectoryDataset createDirectoryDataset(String name, String descr, Path dir) {
        // TODO Auto-generated method stub
        return null;
    }

    public HiveTableDataset createHiveTableDataset(String name, String database, String table) {
        // TODO Auto-generated method stub
        return null;
    }

    public Dataset getDataset(ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Dataset> getDatasets() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Dataset> getDatasets(DatasetCriteria criteria) {
        // TODO Auto-generated method stub
        return null;
    }

}
