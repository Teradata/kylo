package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.controllerservice;

/*-
 * #%L
 * kylo-nifi-teradata-tdch-core
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

import com.thinkbiganalytics.kylo.nifi.teradata.tdch.api.TdchConnectionService;

import org.apache.nifi.controller.AbstractControllerService;

/**
 * A connection service (for testing) that implements {@link TdchConnectionService}<br>
 * This configuration would be similar to a normal deployment
 */
public class DevTdchConnectionService extends AbstractControllerService implements TdchConnectionService {

    @Override
    public String getJdbcDriverClassName() {
        return "com.teradata.jdbc.TeraDriver";
    }

    @Override
    public String getJdbcConnectionUrl() {
        return "jdbc:teradata://localhost";
    }

    @Override
    public String getUserName() {
        return "dbc";
    }

    @Override
    public String getPassword() {
        return "mypwd";
    }

    @Override
    public String getTdchJarPath() {
        return "/usr/lib/tdch/1.5/lib/teradata-connector-1.5.4.jar";
    }

    @Override
    public String getTdchLibraryJarsPath() {
        return "/usr/hdp/current/hive-client/conf";
    }

    @Override
    public String getTdchHadoopClassPath() {
        return "/usr/hdp/current/hive-client/lib";
    }
}
