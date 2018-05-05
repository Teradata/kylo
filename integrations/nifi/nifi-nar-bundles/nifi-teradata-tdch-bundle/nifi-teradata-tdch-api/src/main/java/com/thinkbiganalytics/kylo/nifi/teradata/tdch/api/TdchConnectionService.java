package com.thinkbiganalytics.kylo.nifi.teradata.tdch.api;

/*-
 * #%L
 * nifi-teradata-tdch-api
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

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;

/**
 * Interface for a connection provider service for Teradata used by TDCH
 */
@Tags({"kylo", "thinkbig", "teradata", "tdch", "rdbms", "database", "table"})
@CapabilityDescription("Provides information for connecting to Teradata system for running a TDCH job")
public interface TdchConnectionService extends ControllerService {

    /**
     * The JDBC driver class used by the source and target Teradata plugins when connecting to the Teradata system.
     *
     * @return JDBC driver class
     */
    String getJdbcDriverClassName();

    /**
     * The JDBC url used by the source and target Teradata plugins to connect to the Teradata system.
     *
     * @return JDBC Connection url
     */
    String getJdbcConnectionUrl();

    /**
     * The authentication username used by the source and target Teradata plugins to connect to the Teradata system. Note that the value can include Teradata Wallet references in order to use user name
     * information from the current user's wallet.
     *
     * @return username
     */
    String getUserName();

    /**
     * The authentication password used by the source and target Teradata plugins to connect to the Teradata system. Note that the value can include Teradata Wallet references in order to use user name
     * information from the current user's wallet.
     *
     * @return password
     */
    String getPassword();

    /**
     * The full path to teradata-connector-version.jar. This is referred via $USERLIBTDCH variable in TDCH documentation. Tested with version 1.5.4
     *
     * @return path to teradata-connector-version.jar
     */
    String getTdchJarPath();


    /**
     * The paths to Hadoop and Hive library JARs required by TDCH. This is referred via $LIB_JARS variable in TDCH documentation. Delimiter to use between the entries is a comma (,)
     *
     * @return comma separated strings representing paths to Hadoop and Hive library JARs required by TDCH
     */
    String getTdchLibraryJarsPath();


    /**
     * The paths that constitute Hadoop classpath as required by TDCH. This is referred via $HADOOP_CLASSPATH variable in TDCH documentation. Delimiter to use between the entries is a colon (:)
     *
     * @return colon separated strings representing paths to Hadoop classpath as required by TDCH
     */
    String getTdchHadoopClassPath();
}
