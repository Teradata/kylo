package com.thinkbiganalytics.nifi.v2.sqoop;

/*-
 * #%L
 * thinkbig-nifi-hadoop-service-api
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

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;

/**
 * Interface for a connection provider service for Sqoop<br>
 * Provides information for connecting to a relational system
 */
@Tags({"thinkbig", "ingest", "sqoop", "rdbms", "database", "table"})
@CapabilityDescription("Provides information for connecting to a relational system for running a sqoop job")
public interface SqoopConnectionService extends ControllerService {

    /**
     * Connection string to access source system
     *
     * @return the connection string
     */

    String getConnectionString();

    /**
     * User name for source system authentication
     *
     * @return the user name
     */
    String getUserName();

    /**
     * Get password mode
     *
     * @return the password mode
     */
    PasswordMode getPasswordMode();

    /**
     * Location of encrypted password file on HDFS
     *
     * @return the encrypted password file location in HDFS
     */
    String getPasswordHdfsFile();

    /**
     * Passphrase to decrypt password
     *
     * @return the pass phrase
     */
    String getPasswordPassphrase();

    /**
     * Gets the password
     *
     * @return the password
     */
    String getEnteredPassword();

    /**
     * Connection manager to access source system
     *
     * @return the connection manager string
     */
    String getConnectionManager();

    /**
     * Driver to access source system
     *
     * @return the driver string
     */
    String getDriver();
}

