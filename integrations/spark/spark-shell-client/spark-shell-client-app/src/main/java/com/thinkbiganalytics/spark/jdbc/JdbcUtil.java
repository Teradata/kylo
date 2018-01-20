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

import com.google.common.base.Function;

import org.springframework.jdbc.datasource.DataSourceUtils;

import java.sql.Connection;

import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 * Static support methods for JDBC.
 */
public class JdbcUtil {

    /**
     * Obtains a Connection from a given DataSource.
     */
    private enum DataSourceConnectionFunction implements Function<DataSource, Connection> {
        INSTANCE;

        @Nullable
        @Override
        public Connection apply(@Nullable final DataSource dataSource) {
            //noinspection ConstantConditions
            return DataSourceUtils.getConnection(dataSource);
        }


        @Override
        public String toString() {
            return "JdbcUtil#getDataSourceConnection()";
        }
    }

    /**
     * Returns a function that obtains a Connection from a given DataSource.
     */
    public static Function<DataSource, Connection> getDataSourceConnection() {
        return DataSourceConnectionFunction.INSTANCE;
    }

    /**
     * Instances of {@code JdbcUtil} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private JdbcUtil() {
        throw new UnsupportedOperationException();
    }
}
