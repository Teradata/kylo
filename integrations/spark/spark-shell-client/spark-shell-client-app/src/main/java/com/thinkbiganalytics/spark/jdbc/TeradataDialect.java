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

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.MetadataBuilder;
import org.springframework.stereotype.Component;

import java.sql.Types;

import javax.annotation.Nonnull;

import scala.Option;

/**
 * Dialect for Teradata databases.
 */
@Component
public class TeradataDialect extends Dialect {

    @Override
    public boolean canHandle(@Nonnull final String url) {
        return url.startsWith("jdbc:teradata");
    }

    @Override
    public String createTableAs(@Nonnull String table, @Nonnull String select) {
        return super.createTableAs(table, select) + " WITH DATA";
    }

    @Nonnull
    @Override
    public Converter getConverter(final int sqlType) {
        if (sqlType == Types.TINYINT || sqlType == Types.SMALLINT) {
            return Converters.integerType();
        } else {
            return super.getConverter(sqlType);
        }
    }

    @Nonnull
    @Override
    public Option<DataType> getCatalystType(final int sqlType, @Nonnull final String typeName, final int size, @Nonnull final MetadataBuilder md) {
        if (sqlType == Types.FLOAT) {
            return Option.apply(DataTypes.DoubleType);
        } else {
            //noinspection unchecked
            return (Option) Option.empty();
        }
    }
}
