package com.thinkbiganalytics.hive.service;

import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.kerberos.KerberosTicketGenerator;
import com.thinkbiganalytics.kerberos.KerberosUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Created by sr186054 on 9/13/16.
 */
public class RefreshableDataSource implements DataSource {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RefreshableDataSource.class);

    private AtomicReference<DataSource> target = new AtomicReference<DataSource>();

    private AtomicBoolean isRefreshing = new AtomicBoolean(false);
    String propertyPrefix;


    @Autowired
    Environment env;

    @Inject
    @Qualifier("kerberosHiveConfiguration")
    private KerberosTicketConfiguration kerberosTicketConfiguration;

    public RefreshableDataSource(String propertyPrefix) {
        this.propertyPrefix = propertyPrefix;
    }

    //@PostConstruct
    public void refresh() {
        if (isRefreshing.compareAndSet(false, true)) {
            log.info("REFRESHING DATASOURCE for {} ", propertyPrefix);
            target.set(create());
            isRefreshing.set(false);
        } else {
            //unable to refresh.  Refresh already in progress
        }
    }

    public boolean testConnection() throws SQLException {
        return testConnection(null, null);
    }

    public boolean testConnection(String username, String password) throws SQLException {
        boolean valid = false;
        Connection connection = null;
        Statement statement = null;
        try {
            String prefix = getPrefixWithTrailingDot();
            String query = env.getProperty(prefix + "validationQuery");
            connection = getConnectionForValidation();
            statement = connection.createStatement();
            statement.execute(query);
            valid = true;
        } catch (SQLException e) {
            DataSourceUtils.releaseConnection(connection, this.getDataSource());
            throw e;
        } finally {
            JdbcUtils.closeStatement(statement);
            DataSourceUtils.releaseConnection(connection, this.getDataSource());
        }
        return valid;

    }

    private Connection getConnectionForValidation() throws SQLException {
        refresh();
        return KerberosUtil.getConnectionWithOrWithoutKerberos(getDataSource(), kerberosTicketConfiguration);
    }

    private Connection testAndRefreshIfInvalid() throws SQLException {

        try {
            testConnection();
        } catch (SQLException e) {
            refresh();
        }
        return getConnectionForValidation();
    }

    private Connection testAndRefreshIfInvalid(String username, String password) throws SQLException {

        try {
            testConnection(username, password);
        } catch (SQLException e) {
            refresh();
        }
        return getConnectionForValidation();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return testAndRefreshIfInvalid();


    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return testAndRefreshIfInvalid(username, password);
    }

    //Rest of DataSource methods

    private DataSource getDataSource() {
        return target.get();
    }


    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return getDataSource().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        getDataSource().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        getDataSource().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return getDataSource().getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getDataSource().getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getDataSource().isWrapperFor(iface);
    }

    private String getPrefixWithTrailingDot() {
        String prefix = propertyPrefix.endsWith(".") ? propertyPrefix : propertyPrefix + ".";
        return prefix;
    }

    private DataSource create() {
        String prefix = getPrefixWithTrailingDot();
        boolean userImpersonationEnabled = Boolean.valueOf(env.getProperty("hive.userImpersonation.enabled"));
        String userName = env.getProperty(prefix + "username");
        if(userImpersonationEnabled) {
            String currentUser = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            userName = currentUser;
        }

        String driverClassName = env.getProperty(prefix + "driverClassName");
        String url = env.getProperty(prefix + "url");
        String username = userName;
        String password = env.getProperty(prefix + "password");
      /*  BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driverClassName);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setUrl(url);
        */
        DataSource ds = DataSourceBuilder.create().driverClassName(driverClassName).url(url).username(username).password(password).build();
        return ds;
    }

}