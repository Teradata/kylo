package com.thinkbiganalytics.spark.rest.controller;

/*-
 * #%L
 * kylo-spark-shell-controller
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

import com.thinkbiganalytics.spark.rest.model.ModifiedTransformResponse;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.rest.model.TransformResultModifier;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Modify a TransformResponse and convert it to a new object of type T
 */
public abstract class AbstractTransformResponseModifier<T extends Object> implements TransformResultModifier {


    TransformResponseUtil responseTransformer;

    protected TransformResponse response;


    public String getTableId() {
        return response.getTable();
    }

    /**
     * Wrapper around the TransformRespone object
     * This will call the <code>modifySuccessfulResults()</code> method when successfully returned
     * otherwise it will return the wrapped response in its same state (i.e. PENDING, ERROR)
     * @param response the response to check
     * @return
     */
    public ModifiedTransformResponse<T> modify(TransformResponse response) {
        this.response = response;
        this.responseTransformer = new TransformResponseUtil(response);

        ModifiedTransformResponse modifiedTransformResponse = new ModifiedTransformResponse(response);
        if (response.getStatus() == TransformResponse.Status.SUCCESS) {

            modifySuccessfulResults(modifiedTransformResponse);
        }
        return modifiedTransformResponse;

    }

    protected <C> C getRowValue(String column, List<Object> row, Class<C> type, C defaultValue) {
        return responseTransformer.getRowValue(column, row, type, defaultValue);
    }

    protected <C> C getRowValue(String column, List<Object> row, Class<C> type) throws IllegalArgumentException {
        return responseTransformer.getRowValue(column, row, type);
    }

    @Nullable
    protected Object getRowValue(String column, List<Object> row) {
        return responseTransformer.getRowValue(column, row);
    }


    /**
     * Called the TransformResponse has successfully returned.
     * This is where you can modify the <code>results</code> object in the <code>modifiedTransformResponse</code>
     * @param modifiedTransformResponse the successful response
     */
    public abstract void modifySuccessfulResults(ModifiedTransformResponse<T> modifiedTransformResponse);


}
