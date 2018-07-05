/**
 * 
 */
package com.thinkbiganalytics.metadata.api.catalog;

import java.util.Collection;
import java.util.List;

/**
 *
 */
public interface DataSetProvider {

    List<DataSet> findDataSets();
    
    List<DataSet> findDataSets(DataSource.ID dsId, DataSource.ID... otherIds);
    
    List<DataSet> findDataSets(Collection<DataSource.ID> dsIds);
    
}
