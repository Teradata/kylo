package com.thinkbiganalytics.install.inspector.inspection;

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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Component
public class HiveConnectionInspection extends InspectionBase {

    private static final Logger LOG = LoggerFactory.getLogger(HiveConnectionInspection.class);


    public HiveConnectionInspection() {
        setDocsUrl("/installation/KyloApplicationProperties.html#hive");
        setName("Hive Connection Check");
        setDescription("Checks whether Hive connection is setup");
    }

    @Resource(name = "hiveDataSource")
    private DataSource ds;

    @Override
    public InspectionStatus inspect(Configuration configuration) {
        InspectionStatus status = new InspectionStatus(false);

        try {
            ((com.thinkbiganalytics.hive.service.RefreshableDataSource) ds).testConnection();
        } catch (SQLException e) {
            String msg = String.format("Failed to connect to Hive: %s", e.getMessage());
            LOG.error(msg, e);
            status.addError(msg);
            return status;
        }

        status.setValid(true);
        return status;
    }
}
