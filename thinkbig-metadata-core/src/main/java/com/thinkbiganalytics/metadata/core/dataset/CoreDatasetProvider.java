/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset;

import java.nio.file.Path;
import java.util.List;

import javax.inject.Inject;

import com.thinkbiganalytics.metadata.api.dataset.ChangeSet;
import com.thinkbiganalytics.metadata.api.dataset.DataOperation;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.Dataset.ID;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.event.ChangeEventListener;
import com.thinkbiganalytics.metadata.api.feed.DataDestination;

import reactor.bus.EventBus;

/**
 *
 * @author Sean Felten
 */
public class CoreDatasetProvider implements DatasetProvider {
    
    @Inject
    private EventBus bus;

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.dataset.DatasetProvider#createDataset(java.lang.String, java.lang.String)
     */
    public Dataset createDataset(String name, String descr) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.dataset.DatasetProvider#getChangeSets(com.thinkbiganalytics.metadata.api.dataset.Dataset.ID)
     */
    public List<ChangeSet<?, ?>> getChangeSets(ID dsId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.dataset.DatasetProvider#createOperation(com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset, com.thinkbiganalytics.metadata.api.feed.DataDestination, java.util.List)
     */
    public DataOperation createOperation(DirectoryDataset ds, DataDestination dest, List<Path> paths) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.dataset.DatasetProvider#addListener(com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset, com.thinkbiganalytics.metadata.api.event.ChangeEventListener)
     */
    public void addListener(DirectoryDataset ds, ChangeEventListener<DirectoryDataset, FileList> listener) {
        // TODO Auto-generated method stub

    }

}
