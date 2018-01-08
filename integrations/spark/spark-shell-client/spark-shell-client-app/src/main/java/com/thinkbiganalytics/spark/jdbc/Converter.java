package com.thinkbiganalytics.spark.jdbc;

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

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Converts {@code ResultSet} objects to Spark objects.
 */
public interface Converter {

    /**
     * Converts the object at the specified column in the result set to a Spark object.
     *
     * @param resultSet the JDBC result set
     * @param column    the column index starting at 1
     * @return the Spark object
     * @throws SQLException if the object is unavailable or cannot be converted
     */
    @Nullable
    Object convert(@Nonnull ResultSet resultSet, int column) throws SQLException;
}
