package com.thinkbiganalytics.datalake.authorization;

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

import com.thinkbiganalytics.datalake.authorization.client.SentryClient;
import com.thinkbiganalytics.datalake.authorization.client.SentryClientConfig;
import com.thinkbiganalytics.datalake.authorization.client.SentryClientException;
import com.thinkbiganalytics.datalake.authorization.config.AuthorizationConfiguration;
import com.thinkbiganalytics.datalake.authorization.config.SentryConnection;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;
import com.thinkbiganalytics.datalake.authorization.service.BaseHadoopAuthorizationService;
import com.thinkbiganalytics.kerberos.KerberosTicketGenerator;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sentry Authorization Service
 */

public class SentryAuthorizationService extends BaseHadoopAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(SentryAuthorizationService.class);

    private static final String HADOOP_AUTHORIZATION_TYPE_SENTRY = "SENTRY";
    private static final String HDFS_READ_ONLY_PERMISSION = "read,execute";
    private static final String HIVE_READ_ONLY_PERMISSION = "select";
    private static final String HIVE_REPOSITORY_TYPE = "hive";
    private static final String TABLE = "TABLE";
    private static final String KYLO_POLICY_PREFIX = "kylo";
    private static final String UserGroupObjectError = "Unble to get User Group Information Object. Please check Kerberos settings.";

    SentryClient sentryClientObject;
    SentryConnection sentryConnection;

    private static String convertListToString(List<String> list, String delim) {

        StringBuilder sb = new StringBuilder();

        String loopDelim = "";

        for (String input : list) {
            sb.append(loopDelim);
            sb.append(input.trim());

            loopDelim = delim;
        }
        return sb.toString();
    }

    @Override
    public void initialize(AuthorizationConfiguration config) {

        this.sentryConnection = (SentryConnection) config;
        SentryClientConfig sentryClientConfiguration = new SentryClientConfig(sentryConnection.getDataSource());
        sentryClientConfiguration.setDriverName(sentryConnection.getDriverName());

        /**
         * Static Group Sync Process 
         */
        sentryClientConfiguration.setSentryGroups(sentryConnection.getSentryGroups());

        /**
         * Linux Group Sync Process
         */
        sentryClientConfiguration.setAuthorizationGroupType(sentryConnection.getAuthorizationGroupType());
        sentryClientConfiguration.setLinuxGroupFilePath(sentryConnection.getLinuxGroupFilePath());


        /**
         *  LDAP Sync Process
         */

        sentryClientConfiguration.setLdapContextSource(sentryConnection.getLdapContextSource());
        sentryClientConfiguration.setLdapGroupDnPattern(sentryConnection.getLdapGroupDnPattern());

        this.sentryClientObject = new SentryClient(sentryClientConfiguration);
    }

    @Override
    public HadoopAuthorizationGroup getGroupByName(String groupName) {
        return null;
    }

    @Override
    public List<HadoopAuthorizationGroup> getAllGroups() {
        return sentryClientObject.getAllGroups();
    }

    @Override
    public void createOrUpdateReadOnlyHivePolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, String datebaseName, List<String> tableNames) {

        if (this.sentryConnection.getKerberosTicketConfiguration().isKerberosEnabled()) {
            try {
                UserGroupInformation ugi = authenticatePolicyCreatorWithKerberos(); 
                if(ugi==null){
                    log.error(UserGroupObjectError);
                }
                else    {
                    ugi.doAs(new PrivilegedExceptionAction<Void>() {
                        @Override
                        public Void run() throws Exception {
                            String sentryPolicyName = getHivePolicyName(categoryName, feedName);
                            if (!(sentryClientObject.checkIfRoleExists(sentryPolicyName))) {
                                createReadOnlyHivePolicy(categoryName, feedName, hadoopAuthorizationGroups, datebaseName, tableNames);
                            } else {
                                try {
                                    updateReadOnlyHivePolicy(categoryName, feedName, hadoopAuthorizationGroups, datebaseName, tableNames);
                                } catch (Exception e) {
                                    log.error("Failed to update Hive Policy" + e.getMessage());
                                    throw new RuntimeException(e);
                                }
                            }
                            return null;
                        }
                    });}
            }  
            catch (Exception e) {
                log.error("Error Creating Sentry Hive Policy using Kerberos Authentication" + e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            String sentryPolicyName = getHivePolicyName(categoryName, feedName);
            if (!(sentryClientObject.checkIfRoleExists(sentryPolicyName))) {
                createReadOnlyHivePolicy(categoryName, feedName, hadoopAuthorizationGroups, datebaseName, tableNames);
            } else {
                try {
                    updateReadOnlyHivePolicy(categoryName, feedName, hadoopAuthorizationGroups, datebaseName, tableNames);
                } catch (Exception e) {
                    log.error("Failed to update Hive Policy" + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }

    }

    @Override
    public void createOrUpdateReadOnlyHdfsPolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, List<String> hdfsPaths) {

        if (this.sentryConnection.getKerberosTicketConfiguration().isKerberosEnabled()) {
            try {
                UserGroupInformation ugi = authenticatePolicyCreatorWithKerberos(); 
                if(ugi==null){
                    log.error(UserGroupObjectError);
                }
                else    {
                    ugi.doAs(new PrivilegedExceptionAction<Void>() {
                        @Override
                        public Void run() throws Exception {
                            createReadOnlyHdfsPolicy(categoryName, feedName, hadoopAuthorizationGroups, hdfsPaths);
                            return null;
                        }
                    });}
            } catch (Exception e) {
                log.error("Error Creating Sentry HDFS Policy using Kerberos Authentication" + e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            createReadOnlyHdfsPolicy(categoryName, feedName, hadoopAuthorizationGroups, hdfsPaths);
        }
    }

    @Override
    public void createReadOnlyHivePolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, String datebaseName, List<String> tableNames) {

        /**
         * Create Read Only Policy for Hive - Beeline Approach
         */
        String sentryPolicyName = getHivePolicyName(categoryName, feedName);
        if (sentryClientObject.checkIfRoleExists(sentryPolicyName)) {
            try {
                sentryClientObject.dropRole(sentryPolicyName);
            } catch (SentryClientException e1) {
                log.error("Failed to update policy in sentry" + e1.getMessage());
                throw new RuntimeException(e1);
            }
        }

        try {

            sentryClientObject.createRole(sentryPolicyName);
            for (String groupCounter : hadoopAuthorizationGroups) {
                sentryClientObject.grantRoleToGroup(sentryPolicyName, groupCounter);
            }
            for (String tableCounter : tableNames) {
                sentryClientObject.grantRolePriviledges(HIVE_READ_ONLY_PERMISSION, TABLE, datebaseName + "." + tableCounter, sentryPolicyName);

            }

        } catch (SentryClientException e) {
            log.error("Failed to create Sentry policy" + sentryPolicyName);
            throw new RuntimeException(e);
        }

    }

    @Override
    public void createReadOnlyHdfsPolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, List<String> hdfsPaths) {

        /**
         * Create Read Only Policy for HDFS - ACL Approach
         */
        String hdfsPathForACLCreation = convertListToString(hdfsPaths, ",");
        String groupListStringyfied = convertListToString(hadoopAuthorizationGroups, ",");
        try {
            sentryClientObject.createAcl(sentryConnection.getHadoopConfiguration(), groupListStringyfied, hdfsPathForACLCreation, HDFS_READ_ONLY_PERMISSION);
        } catch (Exception e) {
            log.error("Failed to apply ACL in HDFS Kylo directories " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateReadOnlyHivePolicy(String categoryName, String feedName, List<String> groups, String datebaseName, List<String> tableNames) {

        /**
         * Create Read Only Policy for Hive - Beeline Approach
         */
        String sentryPolicyName = getHivePolicyName(categoryName, feedName);
        try {
            /**
             * Drop Role if exists in Sentry
             */
            if (sentryClientObject.checkIfRoleExists(sentryPolicyName)) {
                sentryClientObject.dropRole(sentryPolicyName);
            }

            sentryClientObject.createRole(sentryPolicyName);
            for (String groupCounter : groups) {
                sentryClientObject.grantRoleToGroup(sentryPolicyName, groupCounter);
            }
            for (String tableCounter : tableNames) {
                sentryClientObject.grantRolePriviledges(HIVE_READ_ONLY_PERMISSION, TABLE, datebaseName + "." + tableCounter, sentryPolicyName);

            }

        } catch (SentryClientException e) {
            log.error("Failed to create Sentry policy" + sentryPolicyName);
            throw new RuntimeException(e);
        }


    }

    @Override
    public void updateReadOnlyHdfsPolicy(String categoryName, String feedName, List<String> groups, List<String> hdfsPaths) {

        /**
         * Update Read Only Policy for HDFS - ACL Approach
         */

        String hdfsPathForACLCreation = convertListToString(hdfsPaths, ",");
        String groupListStringyfied = convertListToString(groups, ",");

        try {
            sentryClientObject.createAcl(sentryConnection.getHadoopConfiguration(), groupListStringyfied, hdfsPathForACLCreation, HDFS_READ_ONLY_PERMISSION);
        } catch (Exception e) {
            log.error("Failed to apply ACL in HDFS Kylo directories " + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    /**
     * If no security policy exists it will be created. If a policy exists it will be updated
     */
    @Override
    public void updateSecurityGroupsForAllPolicies(String categoryName, String feedName, List<String> securityGroupNames, Map<String, Object> feedProperties) {
        if (this.sentryConnection.getKerberosTicketConfiguration().isKerberosEnabled()) {
            try {
                UserGroupInformation ugi = authenticatePolicyCreatorWithKerberos(); 
                if(ugi==null){
                    log.error(UserGroupObjectError);
                }
                else    {
                    ugi.doAs(new PrivilegedExceptionAction<Void>() {
                        @Override
                        public Void run() throws Exception {

                            if (securityGroupNames == null || securityGroupNames.isEmpty()) {

                                // Only delete if the policies exists. It's possibile that someone adds a security group right after feed creation and before initial ingestion
                                String sentryPolicyName = getHivePolicyName(categoryName, feedName);
                                if ((sentryClientObject.checkIfRoleExists(sentryPolicyName))) {
                                    deleteHivePolicy(categoryName, feedName);
                                }
                                if (!StringUtils.isEmpty((String) feedProperties.get(REGISTRATION_HDFS_FOLDERS))) {
                                    String hdfsFoldersWithCommas = ((String) feedProperties.get(REGISTRATION_HDFS_FOLDERS)).replace("\n", ",");
                                    List<String> hdfsFolders = Arrays.asList(hdfsFoldersWithCommas.split(",")).stream().collect(Collectors.toList());
                                    deleteHdfsPolicy(categoryName, feedName, hdfsFolders);
                                }

                            } else {

                                if (!StringUtils.isEmpty((String) feedProperties.get(REGISTRATION_HDFS_FOLDERS))) {
                                    String hdfsFoldersWithCommas = ((String) feedProperties.get(REGISTRATION_HDFS_FOLDERS)).replace("\n", ",");
                                    List<String> hdfsFolders = Arrays.asList(hdfsFoldersWithCommas.split(",")).stream().collect(Collectors.toList());
                                    createReadOnlyHdfsPolicy(categoryName, feedName, securityGroupNames, hdfsFolders);
                                }

                                String sentryHivePolicyName = getHivePolicyName(categoryName, feedName);
                                if (!sentryClientObject.checkIfRoleExists(sentryHivePolicyName)) {
                                    if (!StringUtils.isEmpty((String) feedProperties.get(REGISTRATION_HIVE_TABLES))) {
                                        String hiveTablesWithCommas = ((String) feedProperties.get(REGISTRATION_HIVE_TABLES)).replace("\n", ",");
                                        List<String>
                                        hiveTables =
                                        Arrays.asList(hiveTablesWithCommas.split(",")).stream().collect(Collectors.toList()); //Stream.of(hiveTablesWithCommas).collect(Collectors.toList());
                                        String hiveSchema = ((String) feedProperties.get(REGISTRATION_HIVE_SCHEMA));
                                        createOrUpdateReadOnlyHivePolicy(categoryName, feedName, securityGroupNames, hiveSchema, hiveTables);
                                    }

                                } else {

                                    if (!StringUtils.isEmpty((String) feedProperties.get(REGISTRATION_HIVE_TABLES))) {
                                        try {
                                            sentryClientObject.dropRole(sentryHivePolicyName);
                                        } catch (SentryClientException e) {
                                            log.error("Unable to delete Hive policy  " + sentryHivePolicyName + " in Sentry   " + e.getMessage());
                                            throw new RuntimeException(e);
                                        }

                                        String hiveTablesWithCommas = ((String) feedProperties.get(REGISTRATION_HIVE_TABLES)).replace("\n", ",");
                                        List<String>
                                        hiveTables =
                                        Arrays.asList(hiveTablesWithCommas.split(",")).stream().collect(Collectors.toList()); 
                                        String hiveSchema = ((String) feedProperties.get(REGISTRATION_HIVE_SCHEMA));
                                        List<String> hivePermissions = new ArrayList();
                                        hivePermissions.add(HIVE_READ_ONLY_PERMISSION);
                                        createOrUpdateReadOnlyHivePolicy(categoryName, feedName, securityGroupNames, hiveSchema, hiveTables);
                                    }
                                }
                            }
                            return null;
                        }
                    }); }
            } catch (Exception e) {
                log.error("Error Creating Sentry Policy using Kerberos Authentication" + e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            if (securityGroupNames == null || securityGroupNames.isEmpty()) {

                String sentryPolicyName = getHivePolicyName(categoryName, feedName);
                if ((sentryClientObject.checkIfRoleExists(sentryPolicyName))) {
                    deleteHivePolicy(categoryName, feedName);
                }

                if (!StringUtils.isEmpty((String) feedProperties.get(REGISTRATION_HDFS_FOLDERS))) {
                    String hdfsFoldersWithCommas = ((String) feedProperties.get(REGISTRATION_HDFS_FOLDERS)).replace("\n", ",");
                    List<String> hdfsFolders = Arrays.asList(hdfsFoldersWithCommas.split(",")).stream().collect(Collectors.toList());
                    deleteHdfsPolicy(categoryName, feedName, hdfsFolders);
                }
            } else {

                if (!StringUtils.isEmpty((String) feedProperties.get(REGISTRATION_HDFS_FOLDERS))) {
                    String hdfsFoldersWithCommas = ((String) feedProperties.get(REGISTRATION_HDFS_FOLDERS)).replace("\n", ",");
                    List<String> hdfsFolders = Arrays.asList(hdfsFoldersWithCommas.split(",")).stream().collect(Collectors.toList());
                    createReadOnlyHdfsPolicy(categoryName, feedName, securityGroupNames, hdfsFolders);
                }

                String sentryHivePolicyName = getHivePolicyName(categoryName, feedName);
                if (!sentryClientObject.checkIfRoleExists(sentryHivePolicyName)) {

                    if (!StringUtils.isEmpty((String) feedProperties.get(REGISTRATION_HIVE_TABLES))) {
                        String hiveTablesWithCommas = ((String) feedProperties.get(REGISTRATION_HIVE_TABLES)).replace("\n", ",");
                        List<String> hiveTables = Arrays.asList(hiveTablesWithCommas.split(",")).stream().collect(Collectors.toList()); //Stream.of(hiveTablesWithCommas).collect(Collectors.toList());
                        String hiveSchema = ((String) feedProperties.get(REGISTRATION_HIVE_SCHEMA));

                        createOrUpdateReadOnlyHivePolicy(categoryName, feedName, securityGroupNames, hiveSchema, hiveTables);
                    }
                } else {

                    if (!StringUtils.isEmpty((String) feedProperties.get(REGISTRATION_HIVE_TABLES))) {
                        try {
                            sentryClientObject.dropRole(sentryHivePolicyName);
                        } catch (SentryClientException e) {
                            log.error("Unable to delete Hive policy  " + sentryHivePolicyName + " in Sentry   " + e.getMessage());
                            throw new RuntimeException(e);
                        }
                        String hiveTablesWithCommas = ((String) feedProperties.get(REGISTRATION_HIVE_TABLES)).replace("\n", ",");
                        List<String> hiveTables = Arrays.asList(hiveTablesWithCommas.split(",")).stream().collect(Collectors.toList()); //Stream.of(hiveTablesWithCommas).collect(Collectors.toList());
                        String hiveSchema = ((String) feedProperties.get(REGISTRATION_HIVE_SCHEMA));
                        List<String> hivePermissions = new ArrayList();
                        hivePermissions.add(HIVE_READ_ONLY_PERMISSION);
                        createOrUpdateReadOnlyHivePolicy(categoryName, feedName, securityGroupNames, hiveSchema, hiveTables);
                    }
                }
            }

        }
    }

    @Override
    public void deleteHivePolicy(String categoryName, String feedName) {
        if (this.sentryConnection.getKerberosTicketConfiguration().isKerberosEnabled()) {
            try {

                UserGroupInformation ugi = authenticatePolicyCreatorWithKerberos(); 
                if(ugi==null){
                    log.error(UserGroupObjectError);
                }
                else    {
                    ugi.doAs(new PrivilegedExceptionAction<Void>() {
                        @Override
                        public Void run() throws Exception {
                            String sentryPolicyName = getHivePolicyName(categoryName, feedName);
                            if (sentryClientObject.checkIfRoleExists(sentryPolicyName)) {
                                try {
                                    sentryClientObject.dropRole(sentryPolicyName);
                                } catch (SentryClientException e) {
                                    log.error("Unable to delete policy  " + sentryPolicyName + " in Sentry  " + e.getMessage());
                                    throw new RuntimeException(e);
                                }
                            }
                            return null;
                        }
                    });}
            } catch (Exception e) {
                log.error("Failed to Delete Hive Policy With Kerberos" + e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            String sentryPolicyName = getHivePolicyName(categoryName, feedName);
            if (sentryClientObject.checkIfRoleExists(sentryPolicyName)) {
                try {
                    sentryClientObject.dropRole(sentryPolicyName);
                } catch (SentryClientException e) {
                    log.error("Unable to delete policy  " + sentryPolicyName + " in Sentry  " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }

        }

    }

    @Override
    public void deleteHdfsPolicy(String categoryName, String feedName, List<String> hdfsPaths) {

        /**
         * Delete ACL from list of HDFS Paths
         */
        if (this.sentryConnection.getKerberosTicketConfiguration().isKerberosEnabled()) {
            try {

                UserGroupInformation ugi = authenticatePolicyCreatorWithKerberos(); 
                if(ugi==null){}
                else    {
                    ugi.doAs(new PrivilegedExceptionAction<Void>() {
                        @Override
                        public Void run() throws Exception {
                            String allPathForAclDeletion = convertListToString(hdfsPaths, ",");
                            try {
                                sentryClientObject.flushACL(sentryConnection.getHadoopConfiguration(), allPathForAclDeletion);
                            } catch (Exception e) {
                                log.error("Unable to remove ACL from HDFS Paths" + e.getMessage());
                                throw new RuntimeException(e);
                            }

                            return null;
                        }

                    });
                }
            } catch (Exception e) {
                log.error("Failed to clear HDFS ACL policy with Kerberos" + e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            String allPathForAclDeletion = convertListToString(hdfsPaths, ",");
            try {
                sentryClientObject.flushACL(sentryConnection.getHadoopConfiguration(), allPathForAclDeletion);
            } catch (Exception e) {
                log.error("Unable to remove ACL from HDFS Paths" + e.getMessage());
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public String getType() {
        return HADOOP_AUTHORIZATION_TYPE_SENTRY;
    }

    private String getHivePolicyName(String categoryName, String feedName) {
        return KYLO_POLICY_PREFIX + "_" + categoryName + "_" + feedName + "_" + HIVE_REPOSITORY_TYPE;
    }

    @CheckForNull
    private UserGroupInformation authenticatePolicyCreatorWithKerberos() {
        /**
         * Kerberos Authentication Before Enabling Sentry Policy
         */
        UserGroupInformation userGroupInformation = null;
        if (this.sentryConnection.getKerberosTicketConfiguration().isKerberosEnabled()) {
            KerberosTicketGenerator ticket = new KerberosTicketGenerator();
            try {
                userGroupInformation = ticket.generateKerberosTicket(this.sentryConnection.getKerberosTicketConfiguration());
                log.info("Kerberos Authentication is successfull.");
                return userGroupInformation;
            } catch (IOException e) {
                log.error("Unable to authenticate with Kerberos while creating Sentry Policy  " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return userGroupInformation;
    }
}
