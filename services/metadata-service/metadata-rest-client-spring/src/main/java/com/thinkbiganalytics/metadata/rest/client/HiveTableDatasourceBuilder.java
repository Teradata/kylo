/**
 *
 */
package com.thinkbiganalytics.metadata.rest.client;

/*-
 * #%L
 * thinkbig-metadata-rest-client-spring
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

import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;

/**
 * A builder for HiveTableDatasource models
 */
public interface HiveTableDatasourceBuilder extends DatasourceBuilder<HiveTableDatasourceBuilder, HiveTableDatasource> {

    HiveTableDatasourceBuilder database(String name);

    HiveTableDatasourceBuilder tableName(String name);

    HiveTableDatasourceBuilder modifiers(String mods);

    HiveTableDatasourceBuilder field(String name, String type);

    HiveTableDatasourceBuilder partition(String name, String formula, String value, String... more);
}
