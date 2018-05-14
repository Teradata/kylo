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
 * A dummy connection service that implements {@link TdchConnectionService}
 */
public class DummyTdchConnectionService extends AbstractControllerService implements TdchConnectionService {

    @Override
    public String getJdbcDriverClassName() {
        return "jdbc.driver.class.name";
    }

    @Override
    public String getJdbcConnectionUrl() {
        return "jdbc.connection.url";
    }

    @Override
    public String getUserName() {
        return "user.name";
    }

    @Override
    public String getPassword() {
        return "pass.word";
    }

    @Override
    public String getTdchJarPath() {
        return "/path/to/tdch/jar";
    }

    @Override
    public String getTdchLibraryJarsPath() {
        return "/path/to/library/jars";
    }

    @Override
    public String getTdchHadoopClassPath() {
        return "/hadoop/classpath";
    }
}
