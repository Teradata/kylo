package com.thinkbiganalytics.metadata.api.datasource;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface DatasourceProvider {

    DatasourceCriteria datasetCriteria();

    DerivedDatasource ensureDerivedDatasource(String datasourceType, String identityString, String title, String desc, Map<String, Object> properties);

    DerivedDatasource findDerivedDatasource(String datasourceType, String systemName);



    <D extends Datasource> D ensureDatasource(String name, String descr, Class<D> type);


    DerivedDatasource ensureGenericDatasource(String name, String descr);
    
    Datasource getDatasource(Datasource.ID id);

    void removeDatasource(Datasource.ID id);

    List<Datasource> getDatasources();
    List<Datasource> getDatasources(DatasourceCriteria criteria);

    Datasource.ID resolve(Serializable id);


}
