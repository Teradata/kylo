package com.thinkbiganalytics.nifi.thrift.api;

/*-
 * #%L
 * thinkbig-nifi-hadoop-service-api
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

import java.sql.ResultSet;
import java.util.Date;

/**
 * Intercept iterations over a {@link ResultSet}
 */
public interface RowVisitor {

    /**
     * the visitor method for the given row is called once per row of SQL results
     *
     * @param row a row of SQL results
     */
    void visitRow(ResultSet row);

    /**
     * the visitor method for a column and it's value when the column's colType is compatible with java Date values
     *
     * @param columnName the name of the column
     * @param colType    the SQL type from java.sql.Types
     * @param value      the value for this row and column as a java.util.Date
     */
    void visitColumn(String columnName, int colType, Date value);

    /**
     * the visitor method for a column and it's value when the column's colType is compatible with java String values
     *
     * @param columnName the name of the column
     * @param colType    the SQL type from java.sql.Types
     * @param value      the value for this row and column as a string
     */
    void visitColumn(String columnName, int colType, String value);
}
