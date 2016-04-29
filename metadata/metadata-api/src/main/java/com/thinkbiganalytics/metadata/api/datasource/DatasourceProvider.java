package com.thinkbiganalytics.metadata.api.datasource;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;

public interface DatasourceProvider {

    DatasourceCriteria datasetCriteria();

    Datasource ensureDatasource(String name, String descr);
    DirectoryDatasource ensureDirectoryDatasource(String name, String descr, Path dir);
    HiveTableDatasource ensureHiveTableDatasource(String name, String descr, String database, String table);
    DirectoryDatasource asDirectoryDatasource(Datasource.ID dsId, Path dir);
    HiveTableDatasource asHiveTableDatasource(Datasource.ID dsId, String database, String table);
    
    Datasource getDatasource(Datasource.ID id);
    List<Datasource> getDatasources();
    List<Datasource> getDatasources(DatasourceCriteria criteria);

    Datasource.ID resolve(Serializable id);

}
