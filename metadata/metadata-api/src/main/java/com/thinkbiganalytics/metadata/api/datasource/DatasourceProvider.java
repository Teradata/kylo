package com.thinkbiganalytics.metadata.api.datasource;

import java.io.Serializable;
import java.util.List;

public interface DatasourceProvider {

    DatasourceCriteria datasetCriteria();

    <D extends Datasource> D ensureDatasource(String name, String descr, Class<D> type);
//    HiveTableDatasource ensureHiveTableDatasource(String name, String descr, String database, String table);
//    HiveTableDatasource asHiveTableDatasource(Datasource.ID dsId, String database, String table);

//    DirectoryDatasource ensureDirectoryDatasource(String name, String descr, Path dir);
//    DirectoryDatasource asDirectoryDatasource(Datasource.ID dsId, Path dir);
    
    Datasource getDatasource(Datasource.ID id);
    List<Datasource> getDatasources();
    List<Datasource> getDatasources(DatasourceCriteria criteria);

    Datasource.ID resolve(Serializable id);

}
