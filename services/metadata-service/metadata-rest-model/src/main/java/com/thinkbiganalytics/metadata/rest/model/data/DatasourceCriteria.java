package com.thinkbiganalytics.metadata.rest.model.data;

/*-
 * #%L
 * thinkbig-metadata-rest-model
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

import org.joda.time.DateTime;

public interface DatasourceCriteria {

    String NAME = "name";
    String OWNER = "owner";
    String ON = "on";
    String AFTER = "after";
    String BEFORE = "before";
    String TYPE = "type";

    DatasourceCriteria name(String name);

    DatasourceCriteria createdOn(DateTime time);

    DatasourceCriteria createdAfter(DateTime time);

    DatasourceCriteria createdBefore(DateTime time);

    DatasourceCriteria owner(String owner);

    DatasourceCriteria type(Class<? extends Datasource> type, Class<? extends Datasource>... others);
}
