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


import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

import javax.sql.DataSource;

@Component
public class HiveConnectionInspection extends AbstractInspection {

    private static final Logger LOG = LoggerFactory.getLogger(HiveConnectionInspection.class);

    @Override
    public String getDocsUrl() {
        return "/installation/KyloApplicationProperties.html#hive";
    }

    @Override
    public String getName() {
        return "Hive Connection Check";
    }

    @Override
    public String getDescription() {
        return "Checks whether Hive connection is setup";
    }


    @Override
    public InspectionStatus inspect(Configuration configuration) {
        InspectionStatus status = new InspectionStatus(false);

        DataSource ds;
        try {
            ds = configuration.getServicesBean(HiveConnectionInspectionConfiguration.class, DataSource.class);
        } catch (Exception e) {
            String msg = String.format("Failed to check Hive connection: %s", ExceptionUtils.getRootCause(e).getMessage());
            LOG.error(msg, e);
            status.addError(msg);
            return status;
        }
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
