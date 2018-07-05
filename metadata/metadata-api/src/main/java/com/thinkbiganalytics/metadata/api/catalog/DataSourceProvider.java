/**
 * 
 */
package com.thinkbiganalytics.metadata.api.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public interface DataSourceProvider {

    List<DataSource> findDataSources();
    
    List<DataSource> findDataSources(Connector.ID connId, Connector.ID... moreIds);
    
    List<DataSource> findDataSources(Collection<Connector.ID> connIds);
    
    Optional<DataSource> findDataSource(DataSource.ID id);
}
