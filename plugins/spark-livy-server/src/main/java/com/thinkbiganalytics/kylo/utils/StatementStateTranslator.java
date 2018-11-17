package com.thinkbiganalytics.kylo.utils;

/*-
 * #%L
 * kylo-spark-livy-server
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


import com.thinkbiganalytics.kylo.spark.model.enums.StatementState;
import com.thinkbiganalytics.spark.rest.model.SaveResponse;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

public class StatementStateTranslator {
    private StatementStateTranslator() {
    } // private constructor

    public static TransformResponse.Status translate(StatementState state) {
        switch (state) {
            case available:
                return TransformResponse.Status.SUCCESS;
            case error:
            case cancelled:
            case cancelling:
                return TransformResponse.Status.ERROR;
            default:
                return TransformResponse.Status.PENDING;
        }
    }

    public static SaveResponse.Status translateToSaveResponse(StatementState state) {
        switch (state) {
            case available:
                return SaveResponse.Status.SUCCESS;
            case error:
            case cancelled:
            case cancelling:
                return SaveResponse.Status.ERROR;
            default:
                return SaveResponse.Status.PENDING;
        }
    }
}
