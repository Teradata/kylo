package com.thinkbiganalytics.metadata.api.dataset;

import java.nio.file.Path;
import java.util.Set;

import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;

public interface DatasetProvider {

    DatasetCriteria datasetCriteria();

    Dataset ensureDataset(String name, String descr);
    DirectoryDataset ensureDirectoryDataset(String name, String descr, Path dir);
    HiveTableDataset ensureHiveTableDataset(String name, String descr, String database, String table);
    DirectoryDataset asDirectoryDataset(Dataset.ID dsId, Path dir);
    HiveTableDataset asHiveTableDataset(Dataset.ID dsId, String database, String table);
    
    Dataset getDataset(Dataset.ID id);
    Set<Dataset> getDatasets();
    Set<Dataset> getDatasets(DatasetCriteria criteria);

}
