package com.thinkbiganalytics.metadata.api.dataset;

import java.nio.file.Path;
import java.util.List;

import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.feed.DataDestination;

public interface DatasetProvider {
    
    Dataset createDataset(String name, String descr);

    List<ChangeSet<?, ?>> getChangeSets(Dataset.ID dsId);  // Add criteria filtering
    
    
    DataOperation createOperation(DirectoryDataset ds, DataDestination dest, List<Path> paths);

}
