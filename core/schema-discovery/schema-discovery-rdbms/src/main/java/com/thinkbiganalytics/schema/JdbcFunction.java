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

import java.sql.SQLException;

/**
 * Represents a function that accepts one argument and produces a result.
 *
 * <p>This is a functional interface whose functional method is {@link #apply(Object)}.</p>
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of teh result of the function
 */
public interface JdbcFunction<T, R> {

    /**
     * Applies this function te the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws SQLException if a SQL error occurs
     */
    R apply(T t) throws SQLException;
}
