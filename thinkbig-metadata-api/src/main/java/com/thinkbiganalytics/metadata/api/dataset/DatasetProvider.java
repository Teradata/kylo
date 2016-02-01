package com.thinkbiganalytics.metadata.api.dataset;

import java.nio.file.Path;
import java.util.List;

import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;

public interface DatasetProvider {

    DatasetCriteria datasetCriteria();

    DirectoryDataset createDirectoryDataset(String name, String descr, Path dir);
    HiveTableDataset createHiveTableDataset(String name, String database, String table);
    
    Dataset getDataset(Dataset.ID id);
    List<Dataset> getDatasets();
    List<Dataset> getDatasets(DatasetCriteria criteria);

}
