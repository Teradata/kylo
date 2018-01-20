package com.thinkbiganalytics.spark.datavalidator;

/*-
 * #%L
 * thinkbig-spark-validate-cleanse-app
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

import org.apache.spark.sql.Row;

import java.io.Serializable;

import javax.annotation.Nonnull;

/*
 * Wrapper class to contain our cleansed row and its column validation results
 */
public class CleansedRowResult implements Serializable {

    @Nonnull
    private final Row row;

    @Nonnull
    private final boolean[] columnsValid;

    private final boolean rowIsValid;

    public CleansedRowResult(@Nonnull Row row, @Nonnull boolean[] columnsValid, boolean rowIsValid) {
        this.row = row;
        this.columnsValid = columnsValid;
        this.rowIsValid = rowIsValid;
    }

    @Nonnull
    public Row getRow() {
        return row;
    }

    public boolean isColumnValid(final int index) {
        return columnsValid[index];
    }

    public boolean isRowValid() {
        return rowIsValid;
    }
}
