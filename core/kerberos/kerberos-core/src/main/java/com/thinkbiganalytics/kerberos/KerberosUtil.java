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

import com.google.common.base.Throwables;

import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * Utility methods for executing code that requires an active Kerberos ticket.
 */
public class KerberosUtil {

    private static final Logger log = LoggerFactory.getLogger(KerberosUtil.class);

    /**
     * Gets a connection for the specified data source using the optional Kerberos ticket.
     */
    public static Connection getConnectionWithOrWithoutKerberos(final DataSource dataSource, KerberosTicketConfiguration kerberosTicketConfiguration) throws SQLException {
        return runWithOrWithoutKerberos(new Callable<Connection>() {
            @Override
            public Connection call() throws Exception {
                try {
                    return dataSource.getConnection();
                } catch (final Exception e) {
                    log.error("Error in Kerberos authentication", e);
                    throw new RuntimeException(e);
                }
            }
        }, kerberosTicketConfiguration);
    }

    /**
     * Executes the specified action using the optional Kerberos ticket.
     */
    public static <T> T runWithOrWithoutKerberos(@Nonnull final Callable<T> action, @Nonnull final KerberosTicketConfiguration kerberosTicketConfiguration) {
        try {
            if (kerberosTicketConfiguration.isKerberosEnabled()) {
                log.debug("Running action with Kerberos ticket");
                final KerberosTicketGenerator kerberosTicketGenerator = new KerberosTicketGenerator();
                final UserGroupInformation userGroupInformation = kerberosTicketGenerator.generateKerberosTicket(kerberosTicketConfiguration);
                return userGroupInformation.doAs(new PrivilegedExceptionAction<T>() {
                    @Override
                    public T run() throws Exception {
                        return action.call();
                    }
                });
            } else {
                log.debug("Running action without Kerberos");
                return action.call();
            }
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
