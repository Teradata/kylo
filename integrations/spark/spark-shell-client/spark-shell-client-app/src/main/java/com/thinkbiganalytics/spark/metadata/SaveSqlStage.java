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
import com.thinkbiganalytics.spark.jdbc.DataSourceSupplier;
import com.thinkbiganalytics.spark.jdbc.DefaultDialect;
import com.thinkbiganalytics.spark.jdbc.Dialect;
import com.thinkbiganalytics.spark.model.SaveResult;
import com.thinkbiganalytics.spark.rest.model.JdbcDatasource;

import org.apache.spark.sql.jdbc.JdbcDialect;
import org.apache.spark.sql.jdbc.JdbcDialects$;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * Saves a SQL query to the same data source that it should be executed with.
 */
public class SaveSqlStage implements Supplier<SaveResult> {

    /**
     * SQL data source.
     */
    @Nonnull
    private final Supplier<DataSource> dataSource;

    /**
     * JDBC dialect
     */
    @Nonnull
    private final Dialect dialect;

    /**
     * SQL query.
     */
    @Nonnull
    private final String sql;

    /**
     * Target table name
     */
    @Nonnull
    private final String target;

    /**
     * Constructs a {@code SaveSqlStage} with the specified configuration.
     */
    public SaveSqlStage(@Nonnull final String target, @Nonnull final String sql, @Nonnull final JdbcDatasource datasource) {
        this.dataSource = new DataSourceSupplier(datasource);
        this.sql = sql;
        this.target = target;

        final JdbcDialect jdbcDialect = JdbcDialects$.MODULE$.get(datasource.getDatabaseConnectionUrl());
        this.dialect = (jdbcDialect instanceof Dialect) ? (Dialect) jdbcDialect : new DefaultDialect(jdbcDialect);
    }

    /**
     * Executes the transformation as a CREATE TABLE AS query.
     */
    @Override
    public SaveResult get() {
        new JdbcTemplate(dataSource.get()).execute(dialect.createTableAs(target, sql));
        return new SaveResult();
    }
}
