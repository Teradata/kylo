/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.catalog;

import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;
import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParamsSupplier;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.WrappedNodeMixin;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import javax.jcr.Node;

/**
 *
 */
public interface JcrDataSetSparkParamsSupplier extends WrappedNodeMixin, DataSetSparkParamsSupplier {

    public static final String NODE_TYPE = "tba:DataSetSparkParams";
    public static final String SPARK_PARAMS = "tba:sparkParams";
    
    @Override
    default DataSetSparkParameters getSparkParameters() {
        JcrUtil.getJcrObject(getNode(), JcrDataSetSparkParameters.class);
    }

    @Override
    default DataSetSparkParameters getEffectiveSparkParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    
}
