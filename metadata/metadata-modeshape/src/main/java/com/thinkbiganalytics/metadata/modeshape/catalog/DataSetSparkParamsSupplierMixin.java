/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.catalog;

/*-
 * #%L
 * kylo-metadata-modeshape
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;
import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParamsSupplier;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.WrappedNodeMixin;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public interface DataSetSparkParamsSupplierMixin extends WrappedNodeMixin, DataSetSparkParamsSupplier {

    public static final String NODE_TYPE = "tba:DataSet";
    public static final String SPARK_PARAMS = "tba:sparkParams";
    
    @Override
    default DataSetSparkParameters getSparkParameters() {
        return JcrUtil.getJcrObject(getNode(), SPARK_PARAMS, JcrDataSetSparkParameters.class);
    }

    @Override
    default DataSetSparkParameters getEffectiveSparkParameters() {
        List<DataSetSparkParameters> chain = getSparkParametersChain();
        return new CompositeDataSetSparkParameters(chain);
    }

    default List<DataSetSparkParameters> getSparkParametersChain() {
        return Arrays.asList(getSparkParameters());
    }
}
