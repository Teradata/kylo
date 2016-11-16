package com.thinkbiganalytics.metadata.api.datasource;

import com.thinkbiganalytics.metadata.api.BaseProvider;

import java.util.Set;

/**
 * Created by sr186054 on 11/15/16.
 */
public interface DatasourceDefinitionProvider extends BaseProvider<DatasourceDefinition, DatasourceDefinition.ID> {

    Set<DatasourceDefinition> getDatasourceDefinitions();

    DatasourceDefinition ensureDatasourceDefinition(String processorType);

    DatasourceDefinition findByProcessorType(String processorType);

    void removeAll();


}
