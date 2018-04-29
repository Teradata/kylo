package com.thinkbiganalytics.spark.rest.model;

/*-
 * #%L
 * PageSpec
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

/**
 * Declares a requested page of a dataset.
 */
public class PageSpec {

    /**
     * Index of the first or starting row
     */
    Integer firstRow;
    /**
     * Number of rows inclusive of the first row
     */
    Integer numRows;

    /**
     * Index of the first column
     */
    Integer firstCol;

    /**
     * Number of columns inclusive of the first column
     */
    Integer numCols;

    public Integer getFirstRow() {
        return firstRow;
    }

    public void setFirstRow(Integer firstRow) {
        this.firstRow = firstRow;
    }

    public Integer getNumRows() {
        return numRows;
    }

    public void setNumRows(Integer numRows) {
        this.numRows = numRows;
    }

    public Integer getFirstCol() {
        return firstCol;
    }

    public void setFirstCol(Integer firstCol) {
        this.firstCol = firstCol;
    }

    public Integer getNumCols() {
        return numCols;
    }

    public void setNumCols(Integer numCols) {
        this.numCols = numCols;
    }

    /**
     * Returns specification that indicates no paging requested
     */
    public static PageSpec noPagingSpec() {
        return new PageSpec();
    }

}