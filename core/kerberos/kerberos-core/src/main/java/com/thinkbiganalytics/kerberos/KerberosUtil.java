package com.thinkbiganalytics.kerberos;

/*-
 * #%L
 * thinkbig-kerberos-core
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

import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Created by Jeremy Merrifield on 9/28/16.
 */
public class KerberosUtil {

    private static final Logger log = LoggerFactory.getLogger(KerberosUtil.class);

    public static Connection getConnectionWithOrWithoutKerberos(final DataSource dataSource, KerberosTicketConfiguration kerberosTicketConfiguration) throws SQLException {
        Connection connection = null;
        if (kerberosTicketConfiguration.isKerberosEnabled()) {
            log.info("Initializing Kerberos ticket for Hive connection");
            UserGroupInformation userGroupInformation;
            try {
                KerberosTicketGenerator t = new KerberosTicketGenerator();
                userGroupInformation = t.generateKerberosTicket(kerberosTicketConfiguration);
                connection = userGroupInformation.doAs(new PrivilegedExceptionAction<Connection>() {
                    @Override
                    public Connection run() throws Exception {

                        return dataSource.getConnection();
                    }
                });
            } catch (Exception e) {
                log.error("Error in Kerberos authentication", e);
                throw new RuntimeException(e);
            }
        } else {
            connection = dataSource.getConnection();
        }
        return connection;
    }

}
