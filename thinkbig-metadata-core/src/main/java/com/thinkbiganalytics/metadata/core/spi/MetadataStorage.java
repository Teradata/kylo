/**
 * 
 */
package com.thinkbiganalytics.metadata.core.spi;

import java.nio.file.Path;
import java.util.List;

import com.thinkbiganalytics.metadata.api.dataset.ChangeSet;
import com.thinkbiganalytics.metadata.api.dataset.DataOperation;
import com.thinkbiganalytics.metadata.api.dataset.DataOperation.Result;
import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.feed.Feed;

/**
 *
 * @author Sean Felten
 */
public interface MetadataStorage {

    DirectoryDataset createDirectoryDataset(String name, String descr, Path dir);
    
    DataOperation beginOperation(Feed.ID feedId);
    DataOperation updateOperation(DataOperation.ID id, String status, Result result);
    DataOperation updateOperation(DataOperation.ID id, String status, Exception ex);
    DataOperation updateOperation(DataOperation.ID id, String status, ChangeSet<?, ?> changes);
    
    ChangeSet<DirectoryDataset, FileList> createChangeSet(DirectoryDataset ds, List<Path> paths);
    ChangeSet<HiveTableDataset, HiveTableUpdate> createChangeSet(HiveTableDataset ds, int count);
    
}
