/**
 * 
 */
package com.thinkbiganalytics.metadata.api.catalog;

import com.thinkbiganalytics.metadata.api.Auditable;
import com.thinkbiganalytics.metadata.api.SystemEntity;
import com.thinkbiganalytics.metadata.api.security.AccessControlled;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
public interface Connector extends DataSetSparkParamsSupplier, SystemEntity, Auditable, AccessControlled {
    
    interface ID extends Serializable { }
    
    ID getId();
    
    boolean isActive();
    
    String getPluginId();
    
    String getIcon();
    
    String getColor();
    
    List<DataSource> getDataSources();
}
