package com.thinkbiganalytics.kylo.catalog.table;

/*-
 * #%L
 * kylo-catalog-core
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

import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * Represents a function that performs an action on a {@link DataSource} and returns a result.
 *
 * @param <R> type of result
 */
public interface DataSourceFunction<R> {

    R apply(@Nonnull DataSource fileSystem) throws SQLException;
}
