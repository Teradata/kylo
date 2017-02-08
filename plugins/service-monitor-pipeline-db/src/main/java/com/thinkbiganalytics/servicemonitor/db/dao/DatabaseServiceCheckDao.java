package com.thinkbiganalytics.servicemonitor.db.dao;

/*-
 * #%L
 * thinkbig-service-monitor-pipeline-db
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

import com.thinkbiganalytics.servicemonitor.model.DefaultServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceStatusResponse;
import com.thinkbiganalytics.servicemonitor.model.ServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.util.Assert;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

/**
 */
@Named
public class DatabaseServiceCheckDao implements InitializingBean {


    @Autowired
    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.jdbcTemplate);
    }

    public ServiceStatusResponse healthCheck() {
        String serviceName = "database";
        String componentName = "Kylo Database";
        ServiceComponent component = null;

        Map<String, Object> properties = new HashMap<>();

        try {
            properties =
                (Map<String, Object>) JdbcUtils.extractDatabaseMetaData(jdbcTemplate.getDataSource(), new DatabaseMetaDataCallback() {

                    public Object processMetaData(DatabaseMetaData metadata) throws SQLException, MetaDataAccessException {
                        Map<String, Object> properties = new HashMap<>();
                        properties.put("database", metadata.getDatabaseProductName());
                        properties.put("databaseVersion", metadata.getDatabaseMajorVersion() + "." + metadata.getDatabaseMinorVersion());
                        properties.put("url", metadata.getURL());
                        return properties;
                    }
                });
            String dbVersion = properties.get("database") + " " + properties.get("databaseVersion");
            component =
                new DefaultServiceComponent.Builder(dbVersion, ServiceComponent.STATE.UP)
                    .message(properties.get("database") + " is up.").properties(properties).build();
        } catch (MetaDataAccessException e) {
            component = new DefaultServiceComponent.Builder(componentName, ServiceComponent.STATE.DOWN).exception(e).build();
        }

        return new DefaultServiceStatusResponse(serviceName, Arrays.asList(component));

    }
}
