package com.thinkbiganalytics.feedmgr.service.datasource;

/*-
 * #%L
 * kylo-feed-manager-controller
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

/**
 * Constants for {@code Datasource} objects.
 */
public interface DatasourceConstants {

    /**
     * Name of the 'Database Connection URL' property for {@code DBCPConnectionPool} controller services
     */
    String DATABASE_CONNECTION_URL = "Database Connection URL";

    /**
     * Name of the 'Database Driver Class Name' property for {@code DBCPConnectionPool} controller services
     */
    String DATABASE_DRIVER_CLASS_NAME = "Database Driver Class Name";

    /**
     * Name of the 'Database Driver Location(s)' property for {@code DBCPConnectionPool} controller services
     */
    String DATABASE_DRIVER_LOCATION = "database-driver-locations";

    /**
     * Name of the 'Database User' property for {@code DBCPConnectionPool} controller services
     */
    String DATABASE_USER = "Database User";

    /**
     * Name of the 'Password' property for {@code DBCPConnectionPool} controller services
     */
    String PASSWORD = "Password";
}
