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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

@Component
public class KyloDatabaseConnectionInspection extends InspectionBase {

    private static final String SPRING_DATASOURCE_URL = "spring.datasource.url";
    private static final String SPRING_DATASOURCE_USERNAME = "spring.datasource.username";
    private static final String SPRING_DATASOURCE_PASSWORD = "spring.datasource.password";
    private static final String SPRING_DATASOURCE_DRIVER_CLASS_NAME = "spring.datasource.driverClassName";
    private final Logger LOG = LoggerFactory.getLogger(KyloDatabaseConnectionInspection.class);


    /**
     * This class is a workaround for a problem where DriverManager would not use a class loaded by another classloader
     */
    class DriverShim implements Driver {
        private Driver driver;
        DriverShim(Driver d) {
            this.driver = d;
        }
        @Override
        public boolean acceptsURL(String u) throws SQLException {
            return this.driver.acceptsURL(u);
        }
        @Override
        public Connection connect(String u, Properties p) throws SQLException {
            return this.driver.connect(u, p);
        }
        @Override
        public int getMajorVersion() {
            return this.driver.getMajorVersion();
        }
        @Override
        public int getMinorVersion() {
            return this.driver.getMinorVersion();
        }
        @Override
        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
            return this.driver.getPropertyInfo(u, p);
        }
        @Override
        public boolean jdbcCompliant() {
            return this.driver.jdbcCompliant();
        }

        @Override
        public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return this.driver.getParentLogger();
        }
    }

    public KyloDatabaseConnectionInspection() {
        setDocsUrl("/installation/KyloApplicationProperties.html#kylo");
        setName("Kylo Database Connection Check");
        setDescription("Checks whether Kylo can connect to its own database");
    }

    @Override
    public InspectionStatus inspect(Configuration configuration) {
        LOG.debug("KyloDatabaseConnectionInspection.inspect");
        String url = configuration.getServicesProperty(SPRING_DATASOURCE_URL);
        String username = configuration.getServicesProperty(SPRING_DATASOURCE_USERNAME);
        String password = configuration.getServicesProperty(SPRING_DATASOURCE_PASSWORD);
        String driver = configuration.getServicesProperty(SPRING_DATASOURCE_DRIVER_CLASS_NAME);

        try {
            ClassLoader classloader = configuration.getServicesClassloader();
            Class<Driver> aClass = (Class<Driver>) classloader.loadClass(driver);
            Driver d = aClass.newInstance();
            DriverManager.registerDriver(new DriverShim(d));
        } catch (InstantiationException | IllegalAccessException e) {
            InspectionStatus status = new InspectionStatus(false);
            status.addError(String.format("Failed to create driver '%s': %s", driver, e.getMessage()));
            return status;
        } catch (ClassNotFoundException e) {
            InspectionStatus status = new InspectionStatus(false);
            status.addError(String.format("Failed to load database driver '%s' from kylo-services classpath '%s'. "
                                          + "Ensure you have added driver jar to classpath and jar is readable by kylo-services user", driver, configuration.getServicesClasspath()));
            return status;
        } catch (SQLException e) {
            InspectionStatus status = new InspectionStatus(false);
            status.addError(String.format("Failed to register driver '%s' with DriverManager: %s", driver, e.getMessage()));
            return status;
        }

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            connection.close();
        } catch (SQLException e) {
            InspectionStatus status = new InspectionStatus(false);
            status.addError(String.format("Failed to establish connection to database '%s' with driver '%s' and username '%s': %s. "
                                          + "Check if values are correct for following properties: '%s', '%s', '%s' in %s",
                                          url, driver, username, e.getMessage(),
                                          SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD, configuration.getServicesConfigLocation()));
            return status;
        }

        return new InspectionStatus(true);
    }
}
