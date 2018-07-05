/**
 * 
 */
package com.thinkbiganalytics.metadata.api.catalog;

import com.thinkbiganalytics.metadata.api.security.AccessControlled;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 *
 */
public interface DataSet extends DataSetSparkParamsSupplier, AccessControlled {
    
    interface ID extends Serializable { }

    ID getId();
    
    String getTitle();
    
    String getDescription();

    DateTime getCreatedTime();

    DateTime getModifiedTime();

    DataSource getDataSource();
}
