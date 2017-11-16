package com.thinkbiganalytics.spark.metadata;

/*-
 * #%L
 * kylo-spark-shell-client-app
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
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

import com.google.common.base.Supplier;
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;
import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.model.TransformResult;

import org.apache.spark.storage.StorageLevel;

import java.util.Arrays;

import javax.annotation.Nonnull;

/**
 * Creates a transform result from a shell transformation.
 */
public class ShellTransformStage implements Supplier<TransformResult> {

    /**
     * Data set from shell transformation.
     */
    @Nonnull
    private final DataSet dataSet;

    /**
     * Constructs a {@code ShellTransformStage}.
     */
    public ShellTransformStage(@Nonnull final DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public TransformResult get() {
        final TransformResult result = new TransformResult();
        result.setDataSet(dataSet.persist(StorageLevel.MEMORY_ONLY()));
        result.setColumns(Arrays.<QueryResultColumn>asList(new QueryResultRowTransform(result.getDataSet().schema(), "").columns()));
        return result;
    }
}
