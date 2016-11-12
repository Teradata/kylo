package com.thinkbiganalytics.kerberos;

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

    private static Logger log = LoggerFactory.getLogger(KerberosUtil.class);

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
