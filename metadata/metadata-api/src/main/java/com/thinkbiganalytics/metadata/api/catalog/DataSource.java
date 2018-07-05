/**
 * 
 */
package com.thinkbiganalytics.metadata.api.catalog;

import com.thinkbiganalytics.metadata.api.security.AccessControlled;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
public interface DataSource extends DataSetSparkParamsSupplier, AccessControlled {
    
    interface ID extends Serializable { }

    ID getId();
    
    String getTitle();
    
    String getDescription();

    DateTime getCreatedTime();

    DateTime getModifiedTime();

    Connector getConnector();
    
    List<DataSet> getDataSets();
}
