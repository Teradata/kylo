package com.thinkbiganalytics.datalake.authorization.config;

/*-
 * #%L
 * thinkbig-hadoop-authorization-sentry
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

import com.thinkbiganalytics.datalake.authorization.SentryAuthorizationService;
import com.thinkbiganalytics.datalake.authorization.service.HadoopAuthorizationService;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * Sentry Configuration
 */
@Configuration
@PropertySource("classpath:authorization.sentry.properties")
public class SentryConfiguration {

    @Bean(name = "hadoopAuthorizationService")
    public HadoopAuthorizationService getAuthorizationService(@Value("${beeline.connection.url}") String connectionURL
                                                              , @Value("${beeline.drive.name}") String driverURL
                                                              , @Value("${beeline.userName}") String userName
                                                              , @Value("${beeline.password}") String password
                                                              , @Value("${hdfs.hadoop.configuration}") String hadoopConfiguration
                                                              , @Value("${authorization.sentry.type}") String authorizationGroupType
                                                              , @Value("${authorization.sentry.unix.group.filePath}") String linuxGroupFilePath
                                                              , @Value("${authorization.sentry.ldap.url}") String ldapURL
                                                              , @Value("${authorization.sentry.ldap.authDn}") String ldapAdmin
                                                              , @Value("${authorization.sentry.ldap.password}") String ldapPassword
                                                              , @Value("${authorization.sentry.ldap.authenticator.groupDnPatterns}") String groupDnPattern
                                                              , @Value("${authorization.sentry.groups}") String sentryGroups
                                                              , @Value("${sentry.kerberos.principal}") String kerberosPrincipal
                                                              , @Value("${sentry.kerberos.KeytabLocation}") String kerberosKeytabLocation
                                                              , @Value("${sentry.IsKerberosEnabled}") String kerberosEnabled) {    
        SentryConnection sentryConnection = new SentryConnection();
        sentryConnection.setDriverName(driverURL);
        sentryConnection.setSentryGroups(sentryGroups);
        sentryConnection.setHadoopConfiguration(hadoopConfiguration);
        sentryConnection.setDataSource(dataSource(connectionURL, userName, password));
        sentryConnection.setKerberosTicketConfiguration(createKerberosTicketConfiguration(kerberosEnabled, hadoopConfiguration, kerberosPrincipal, kerberosKeytabLocation));
        sentryConnection.setAuthorizationGroupType(authorizationGroupType); 
        sentryConnection.setLinuxGroupFilePath(linuxGroupFilePath);
        sentryConnection.setLdapContextSource(getLdapContextSource(ldapURL,ldapAdmin,ldapPassword));
        sentryConnection.setLdapGroupDnPattern(groupDnPattern);

        SentryAuthorizationService hadoopAuthorizationService = new SentryAuthorizationService();
        hadoopAuthorizationService.initialize(sentryConnection);
        return hadoopAuthorizationService;
    }

    public DataSource dataSource(String connectionURL
                                 , String userName
                                 , String password) {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(connectionURL);
        dataSourceBuilder.username(userName);
        dataSourceBuilder.password(password);
        return dataSourceBuilder.build();
    }

    /**
     * Create LDAP Context for Authentication
     * @param ldapURL LDAP URL
     * @param ldapAdminDN LDAP User For Search Query.
     * @param ldapAdminPassword LDAP Password for specified user.
     * @return
     */
    public LdapContextSource getLdapContextSource(String ldapURL , String ldapAdminDN , String ldapAdminPassword ) {

        LdapContextSource lcs = new LdapContextSource();
        lcs.setUrl(ldapURL);
        lcs.setUserDn(ldapAdminDN);
        lcs.setPassword(ldapAdminPassword);
        lcs.setDirObjectFactory(DefaultDirObjectFactory.class);
        lcs.afterPropertiesSet();
        return lcs;
    }

    private KerberosTicketConfiguration createKerberosTicketConfiguration(String kerberosEnabled, String hadoopConfigurationResources, String kerberosPrincipal, String keytabLocation) {
        KerberosTicketConfiguration config = new KerberosTicketConfiguration();
        config.setKerberosEnabled("true".equalsIgnoreCase(kerberosEnabled) ? true : false);
        config.setHadoopConfigurationResources(hadoopConfigurationResources);
        config.setKerberosPrincipal(kerberosPrincipal);
        config.setKeytabLocation(keytabLocation);
        return config;
    }

}
