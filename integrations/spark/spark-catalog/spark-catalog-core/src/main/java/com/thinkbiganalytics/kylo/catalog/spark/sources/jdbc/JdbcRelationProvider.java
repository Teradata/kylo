package com.thinkbiganalytics.kylo.catalog.spark.sources.jdbc;

/*-
 * #%L
 * Kylo Catalog Core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.BaseRelation;
import org.apache.spark.sql.sources.DataSourceRegister;
import org.apache.spark.sql.sources.RelationProvider;
import org.apache.spark.util.Utils;

import java.util.ServiceLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.collection.immutable.Map;

/**
 * Factory for Spark data sources that can read from and write to JDBC tables.
 */
public class JdbcRelationProvider implements RelationProvider {

    @Nonnull
    @Override
    public BaseRelation createRelation(@Nonnull final SQLContext sqlContext, @Nonnull final Map<String, String> parameters) {
        final RelationProvider provider = findProvider();
        if (provider != null) {
            final BaseRelation relation = provider.createRelation(sqlContext, parameters);
            return new JdbcRelation(relation, Thread.currentThread().getContextClassLoader());
        } else {
            throw new IllegalStateException("Cannot find Spark JDBC relation provider");
        }
    }

    /**
     * Locates Spark's JDBC relation provider.
     */
    @Nullable
    private RelationProvider findProvider() {
        final ServiceLoader<DataSourceRegister> serviceLoader = ServiceLoader.load(DataSourceRegister.class, Utils.getContextOrSparkClassLoader());

        for (final DataSourceRegister register : serviceLoader) {
            if ("jdbc".equalsIgnoreCase(register.shortName()) && register instanceof RelationProvider) {
                return (RelationProvider) register;
            }
        }

        return null;
    }
}
