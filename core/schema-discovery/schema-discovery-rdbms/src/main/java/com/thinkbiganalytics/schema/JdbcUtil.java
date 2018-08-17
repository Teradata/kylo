package com.thinkbiganalytics.schema;

/*-
 * #%L
 * kylo-schema-discovery-rdbms
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Static utility methods for handling JDBC results.
 */
public class JdbcUtil {

    /**
     * Transforms the specified result set using the specified function.
     */
    @Nonnull
    public static <T> List<T> transformResults(@Nonnull final ResultSet resultSet, @Nonnull final JdbcFunction<ResultSet, T> function) throws SQLException {
        final List<T> results = new ArrayList<>();

        while (resultSet.next()) {
            results.add(function.apply(resultSet));
        }

        return results;
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
