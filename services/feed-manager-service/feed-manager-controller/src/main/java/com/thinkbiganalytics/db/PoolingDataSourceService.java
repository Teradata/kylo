package com.thinkbiganalytics.db;


import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

/**
 * simple Connection Pooling service to return a new DataSource
 * Used for the DBSchemaParser class
 */
public class PoolingDataSourceService {

    /**
     * Cache of datasources
     */
    private static Map<String, DataSource> datasources = new ConcurrentHashMap<>();

    public static class DataSourceProperties {

        String user;
        String password;
        String url;
        boolean testOnBorrow;
        String validationQuery;

        public DataSourceProperties(String user, String password, String url) {
            this.user = user;
            this.password = password;
            this.url = url;
        }

        public DataSourceProperties(String user, String password, String url, boolean testOnBorrow, String validationQuery) {
            this.user = user;
            this.password = password;
            this.url = url;
            this.testOnBorrow = testOnBorrow;
            this.validationQuery = validationQuery;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isTestOnBorrow() {
            return testOnBorrow;
        }

        public void setTestOnBorrow(boolean testOnBorrow) {
            this.testOnBorrow = testOnBorrow;
        }

        public String getValidationQuery() {
            return validationQuery;
        }

        public void setValidationQuery(String validationQuery) {
            this.validationQuery = validationQuery;
        }
    }


    public static DataSource getDataSource(DataSourceProperties props) {
        String cacheKey = cacheKey(props.getUrl(), props.getUser(), props.getPassword());
        return datasources.computeIfAbsent(cacheKey, key -> createDatasource(props));
    }


    private static DataSource createDatasource(DataSourceProperties props) {
        String cacheKey = cacheKey(props.getUrl(), props.getUser(), props.getPassword());
        DataSource ds = DataSourceBuilder.create().url(props.getUrl()).username(props.getUser()).password(props.getPassword()).build();
        if (props.isTestOnBorrow() && StringUtils.isNotBlank(props.getValidationQuery())) {
            if (ds instanceof org.apache.tomcat.jdbc.pool.DataSource) {
                ((org.apache.tomcat.jdbc.pool.DataSource) ds).setTestOnBorrow(true);
                ((org.apache.tomcat.jdbc.pool.DataSource) ds).setValidationQuery(props.getValidationQuery());
            } else if (ds instanceof org.apache.commons.dbcp2.BasicDataSource) {
                ((org.apache.commons.dbcp2.BasicDataSource) ds).setValidationQuery(props.getValidationQuery());
                ((org.apache.commons.dbcp2.BasicDataSource) ds).setTestOnBorrow(true);
            } else if (ds instanceof org.apache.commons.dbcp.BasicDataSource) {
                ((org.apache.commons.dbcp.BasicDataSource) ds).setValidationQuery(props.getValidationQuery());
                ((org.apache.commons.dbcp.BasicDataSource) ds).setTestOnBorrow(true);
            }
        }
        return ds;
    }

    private static String cacheKey(String connectURI, String user, String password) {
        return connectURI + "." + user + "." + password;
    }

}