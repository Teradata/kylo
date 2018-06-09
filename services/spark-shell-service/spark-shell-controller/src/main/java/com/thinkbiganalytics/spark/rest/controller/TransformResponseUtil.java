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

import com.thinkbiganalytics.discovery.schema.QueryResultColumn;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

/**
 * Transform the row results coming from the spark shell into Java Objects
 * provide easy access to Row values
 */
public class TransformResponseUtil {

    protected Map<String, Integer> hiveColumnLabelIndex;

    protected TransformResponse response;

    public TransformResponseUtil(TransformResponse response) {
        this.response = response;
        if (response.getStatus() == TransformResponse.Status.SUCCESS) {
            this.buildColumnIndexMap();
        }
    }

    protected void buildColumnIndexMap() {
        if (response.getResults() != null && response.getResults().getColumns() != null) {
            List<QueryResultColumn> columnList = response.getResults().getColumns();
            hiveColumnLabelIndex = IntStream.range(0, columnList.size())
                .boxed()
                .collect(Collectors.toMap((i) -> columnList.get(i).getHiveColumnLabel(), i -> i));
        }
    }

    @Nullable
    public Object getRowValue(String column, List<Object> row) {
        if (hiveColumnLabelIndex != null) {
            Integer index = hiveColumnLabelIndex.get(column);
            if (index != null) {
                return row.get(index);
            }
        }
        return null;
    }

    public void setRowValue(String column, List<Object> row, String value) {
        if (hiveColumnLabelIndex != null) {
            Integer index = hiveColumnLabelIndex.get(column);
            if (index != null) {
                row.add(index, value);
            }
        }
    }


    public <C> C getRowValue(String column, List<Object> row, Class<C> type, C defaultValue) {
        C value = null;
        try {
            value = getRowValue(column, row, type);
        } catch (IllegalArgumentException e) {
            value = defaultValue;
        }
        return value;
    }

    public <C> C getRowValue(String column, List<Object> row, Class<C> type) throws IllegalArgumentException {
        Object o = getRowValue(column, row);
        if (o != null) {
            if (type.isInstance(o)) {
                return (C) o;
            } else if (type.isAssignableFrom(String.class)) {
                return (C) o.toString();
            } else if (o instanceof String) {
                try {
                    return (C) type.getDeclaredMethod("valueOf", String.class).invoke(null, o.toString());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Cannot convert " + o.toString() + " to " + type);
                }
            } else {
                throw new IllegalArgumentException("Cannot convert " + o.toString() + " to " + type);
            }
        } else {
            return null;
        }
    }


}
