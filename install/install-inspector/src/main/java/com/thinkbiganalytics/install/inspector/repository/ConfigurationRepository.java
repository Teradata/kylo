package com.thinkbiganalytics.install.inspector.repository;

/*-
 * #%L
 * kylo-install-inspector
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


import com.thinkbiganalytics.install.inspector.inspection.Configuration;
import com.thinkbiganalytics.install.inspector.inspection.Path;

import org.springframework.stereotype.Repository;

@Repository
public class ConfigurationRepository {

    private Configuration configuration;

    public Configuration get(int id) {
        return configuration;
    }

    public Configuration create(Path path) {
        configuration = new Configuration(0, path);
        return configuration;
    }
}
