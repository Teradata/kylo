package com.thinkbiganalytics.metadata.api.datasource;

import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 11/10/16.
 */
public interface DerivedDatasource extends Datasource {

    void setTitle(String title);

    String getTitle();

    void setProperties(Map<String, Object> properties);

    Map<String, Object> getProperties();

    Set<DatasourceDefinition> getDatasourceDefinitions();

    String getDatasourceType();

}
