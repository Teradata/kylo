package com.thinkbiganalytics.datalake.authorization.client;

/*-
 * #%L
 * thinkbig-sentry-client
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

import javax.sql.DataSource;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * Sentry Client configuration class for setting sentry connection information.
 */
public class SentryClientConfig {

    private String driverName;
    private String connectionString;
    private Configuration config;
    private String username;
    private String password;
    private String keyTab;
    private String principal;
    private FileSystem fileSystem;
    private DataSource dataSrouce;
    private JdbcTemplate sentryJdbcTemplate;
    private String sentryGroups;
    private String authorizationGroupType;
    private String linuxGroupFilePath;


    /**
     * LDAP Connection Parameter 
     */
    private String ldapURL;
    private String ldapAdmin;
    private String ldapAdminPassword;
    private LdapContextSource ldapContextSource;
    private String ldapGroupDnPattern;



    public SentryClientConfig() {

    }

    public SentryClientConfig(String driverName, String connectionString) {
        this.driverName = driverName;
        this.connectionString = connectionString;
    }

    public SentryClientConfig(String driverName, String connectionString, String username, String password) {
        this.username = driverName;
        this.connectionString = connectionString;
        this.setUsername(username);
        this.setPassword(password);
    }

    public SentryClientConfig(DataSource dataSrouce) {
        this.dataSrouce = dataSrouce;
        setSentryJdbcTemplate(new JdbcTemplate(this.dataSrouce));
    }

    public JdbcTemplate getSentryJdbcTemplate() {
        return sentryJdbcTemplate;
    }


    public void setSentryJdbcTemplate(JdbcTemplate sentryJdbcTemplate) {
        this.sentryJdbcTemplate = sentryJdbcTemplate;
    }


    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public String getKeyTab() {
        return keyTab;
    }

    public void setKeyTab(String keyTab) {
        this.keyTab = keyTab;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DataSource getDataSrouce() {
        return dataSrouce;
    }

    public void setDataSrouce(DataSource dataSrouce) {
        this.dataSrouce = dataSrouce;
    }

    public String getSentryGroups() {
        return sentryGroups;
    }

    public void setSentryGroups(String sentryGroups) {
        this.sentryGroups = sentryGroups;
    }

    public String getAuthorizationGroupType() {
        return authorizationGroupType;
    }

    public void setAuthorizationGroupType(String authorizationGroupType) {
        this.authorizationGroupType = authorizationGroupType;
    }

    public String getLinuxGroupFilePath() {
        return linuxGroupFilePath;
    }

    public void setLinuxGroupFilePath(String linuxGroupFilePath) {
        this.linuxGroupFilePath = linuxGroupFilePath;
    }

    public String getLdapURL() {
        return ldapURL;
    }

    public void setLdapURL(String ldapURL) {
        this.ldapURL = ldapURL;
    }

    public String getLdapAdmin() {
        return ldapAdmin;
    }

    public void setLdapAdmin(String ldapAdmin) {
        this.ldapAdmin = ldapAdmin;
    }

    public String getLdapAdminPassword() {
        return ldapAdminPassword;
    }

    public void setLdapAdminPassword(String ldapAdminPassword) {
        this.ldapAdminPassword = ldapAdminPassword;
    }

    public String getLdapGroupDnPattern() {
        return ldapGroupDnPattern;
    }

    public void setLdapGroupDnPattern(String ldapGroupDnPattern) {
        this.ldapGroupDnPattern = ldapGroupDnPattern;
    }

    public LdapContextSource getLdapContextSource() {
        return ldapContextSource;
    }

    public void setLdapContextSource(LdapContextSource ldapContextSource) {
        this.ldapContextSource = ldapContextSource;
    }

}
