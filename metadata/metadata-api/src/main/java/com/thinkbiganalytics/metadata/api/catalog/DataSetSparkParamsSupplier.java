/**
 * 
 */
package com.thinkbiganalytics.metadata.api.catalog;

/**
 *
 */
public interface DataSetSparkParamsSupplier {

    DataSetSparkParameters getSparkParameters();
    
    DataSetSparkParameters getEffectiveSparkParameters();

}
