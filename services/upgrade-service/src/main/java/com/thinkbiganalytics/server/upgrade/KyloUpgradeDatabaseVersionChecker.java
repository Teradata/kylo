package com.thinkbiganalytics.server.upgrade;

/*-
 * #%L
 * thinkbig-service-app
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

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.KyloVersionUtil;
import com.thinkbiganalytics.db.PoolingDataSourceService;
import com.thinkbiganalytics.feedmgr.service.EncryptionService;
import com.thinkbiganalytics.server.KyloUpgradeDatabaseVersionConfig;
import com.thinkbiganalytics.spring.SpringEnvironmentProperties;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;


@Configuration
@EnableConfigurationProperties
@PropertySource("classpath:application.properties")
@Import(KyloUpgradeDatabaseVersionConfig.class)
public class KyloUpgradeDatabaseVersionChecker {

    private static final Logger log = LoggerFactory.getLogger(KyloUpgradeDatabaseVersionChecker.class);

    @Autowired
    SpringEnvironmentProperties environmentProperties;

    @Autowired
    EncryptionService encryptionService;

    public KyloUpgradeDatabaseVersionChecker() {

    }

    /**
     * Query the Database for the Kylo Version
     *
     * @return the KyloVersion from the database, or null if not found or if an error occurs
     */
    public KyloVersion getDatabaseVersion() {
        KyloVersion version = null;
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            String user = environmentProperties.getPropertyValueAsString("spring.datasource.username");
            String password = environmentProperties.getPropertyValueAsString("spring.datasource.password");
            password = encryptionService.isEncrypted(password) ? encryptionService.decrypt(password) : password;
            String uri = environmentProperties.getPropertyValueAsString("spring.datasource.url");
            String driverClassName = environmentProperties.getPropertyValueAsString("spring.datasource.driverClassName");
            boolean testOnBorrow = BooleanUtils.toBoolean(environmentProperties.getPropertyValueAsString("spring.datasource.testOnBorrow"));
            String validationQuery = environmentProperties.getPropertyValueAsString("spring.data.validationQuery");

            PoolingDataSourceService.DataSourceProperties dataSourceProperties = new PoolingDataSourceService.DataSourceProperties(user, password, uri, driverClassName, testOnBorrow, validationQuery);

            DataSource dataSource = PoolingDataSourceService.getDataSource(dataSourceProperties);

            connection = dataSource.getConnection();
            String query = "SELECT MAJOR_VERSION,MINOR_VERSION FROM kylo.KYLO_VERSION ";
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                String majorVersion = rs.getString("MAJOR_VERSION");
                String minorVersion = rs.getString("MINOR_VERSION");
                version = new KyloVersionUtil.Version(majorVersion, minorVersion);
            }

        } catch (SQLException e) {
            // this is ok.. If an error happens assume the upgrade is needed.  The method will return a null value if errors occur and the upgrade app will start.
        } finally {
            JdbcUtils.closeStatement(statement);
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeConnection(connection);
        }
        return version;

    }

}
