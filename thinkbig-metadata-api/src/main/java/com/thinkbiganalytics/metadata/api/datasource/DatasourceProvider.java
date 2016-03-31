package com.thinkbiganalytics.metadata.api.datasource;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDataset;

public interface DatasourceProvider {

    DatasourceCriteria datasetCriteria();

    Datasource ensureDataset(String name, String descr);
    DirectoryDataset ensureDirectoryDataset(String name, String descr, Path dir);
    HiveTableDataset ensureHiveTableDataset(String name, String descr, String database, String table);
    DirectoryDataset asDirectoryDataset(Datasource.ID dsId, Path dir);
    HiveTableDataset asHiveTableDataset(Datasource.ID dsId, String database, String table);
    
    Datasource getDataset(Datasource.ID id);
    Set<Datasource> getDatasets();
    List<Datasource> getDatasets(DatasourceCriteria criteria);

    Datasource.ID resolve(Serializable id);

}
