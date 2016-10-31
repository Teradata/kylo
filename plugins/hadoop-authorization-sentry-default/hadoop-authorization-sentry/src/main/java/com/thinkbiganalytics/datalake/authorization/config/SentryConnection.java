package com.thinkbiganalytics.datalake.authorization.config;

import javax.sql.DataSource;

import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;

/**
 * Created by Shashi Vishwakarma on 20/9/16.
 */

public class SentryConnection implements AuthorizationConfiguration {

    private String connectionURL;
    private String driverName;
    private String username;
    private String password;
    private DataSource dataSource;
    private String hadoopConfiguration;
    private String sentryGroups;
    private KerberosTicketConfiguration kerberosTicketConfiguration;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getHadoopConfiguration() {
        return hadoopConfiguration;
    }

    public void setHadoopConfiguration(String hadoopConfiguration) {
        this.hadoopConfiguration = hadoopConfiguration;
    }

    public String getSentryGroups() {
        return sentryGroups;
    }

    public void setSentryGroups(String sentryGroups) {
        this.sentryGroups = sentryGroups;
    }

    public KerberosTicketConfiguration getKerberosTicketConfiguration() {
        return kerberosTicketConfiguration;
    }

    public void setKerberosTicketConfiguration(KerberosTicketConfiguration kerberosTicketConfiguration) {
        this.kerberosTicketConfiguration = kerberosTicketConfiguration;
    }

}
