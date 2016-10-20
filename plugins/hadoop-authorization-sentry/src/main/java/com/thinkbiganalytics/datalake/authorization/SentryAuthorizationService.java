package com.thinkbiganalytics.datalake.authorization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.datalake.authorization.client.SentryClient;
import com.thinkbiganalytics.datalake.authorization.client.SentryClientConfig;
import com.thinkbiganalytics.datalake.authorization.client.SentryClientException;
import com.thinkbiganalytics.datalake.authorization.config.AuthorizationConfiguration;
import com.thinkbiganalytics.datalake.authorization.config.SentryConnection;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;
import com.thinkbiganalytics.datalake.authorization.service.BaseHadoopAuthorizationService;

/**
 * Sentry Authorization Service
 *
 * Created by Shashi Vishwakarma on 19/9/2016.
 * 
 */
public class SentryAuthorizationService  extends BaseHadoopAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(SentryAuthorizationService.class);

    private static final String HADOOP_AUTHORIZATION_TYPE_SENTRY = "SENTRY";
    private static final String HDFS_READ_ONLY_PERMISSION="read,execute";
    private static final String HIVE_READ_ONLY_PERMISSION="select";
    private static final String HIVE_REPOSITORY_TYPE = "hive";
    private static final String TABLE = "TABLE";
    private static final String KYLO_POLICY_PREFIX = "kylo";


    SentryClient sentryClientObject;
    SentryConnection sentryConnection ;

    @Override
    public void initialize(AuthorizationConfiguration config) {

        this.sentryConnection = (SentryConnection) config;
        SentryClientConfig sentryClientConfiguration = new SentryClientConfig(sentryConnection.getDataSource());
        sentryClientConfiguration.setDriverName(sentryConnection.getDriverName());
        sentryClientConfiguration.setSentryGroups(sentryConnection.getSentryGroups());
        this.sentryClientObject = new SentryClient(sentryClientConfiguration);
    }

    @Override
    public HadoopAuthorizationGroup getGroupByName(String groupName) {
        return null;
    }

    @Override
    public List<HadoopAuthorizationGroup> getAllGroups() {
        return  sentryClientObject.getAllGroups(); 
    }

    @Override
    public void createOrUpdateReadOnlyHivePolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, String datebaseName, List<String> tableNames) {

        String sentryPolicyName = getHivePolicyName(categoryName, feedName);
        if(!(sentryClientObject.checkIfRoleExists(sentryPolicyName)))
        {
            createReadOnlyHivePolicy(categoryName, feedName, hadoopAuthorizationGroups, datebaseName, tableNames);
        }
        else
        {
            try {
                updateReadOnlyHivePolicy(categoryName, feedName, hadoopAuthorizationGroups, datebaseName, tableNames);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update Hive Policy" + e.getMessage());
            }
        }

    }

    @Override
    public void createOrUpdateReadOnlyHdfsPolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, List<String> hdfsPaths) {
        createReadOnlyHdfsPolicy(categoryName, feedName, hadoopAuthorizationGroups, hdfsPaths);
    }

    @Override
    public void createReadOnlyHivePolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, String datebaseName, List<String> tableNames) {

        /**
         * Create Read Only Policy for Hive - Beeline Approach
         */
        String sentryPolicyName = getHivePolicyName(categoryName, feedName);
        if(sentryClientObject.checkIfRoleExists(sentryPolicyName))
            try {
                sentryClientObject.dropRole(sentryPolicyName);
            } catch (SentryClientException e1) {
                throw new RuntimeException("Failed to update policy in sentry" + e1.getMessage());
            }

        try {

            sentryClientObject.createRole(sentryPolicyName);
            for( String groupCounter : hadoopAuthorizationGroups)
            {
                sentryClientObject.grantRoleToGroup( sentryPolicyName, groupCounter);
            }
            for(String tableCounter : tableNames)
            {
                sentryClientObject.grantRolePriviledges( HIVE_READ_ONLY_PERMISSION, TABLE, datebaseName+"."+tableCounter, sentryPolicyName);

            }

        } catch (SentryClientException e) {
            throw new RuntimeException("Failed to create Sentry policy" + sentryPolicyName);
        }

    }

    @Override
    public void createReadOnlyHdfsPolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, List<String> hdfsPaths) {

        /**
         * Create Read Only Policy for HDFS - ACL Approach
         */
        String hdfsPathForACLCreation =  convertListToString(hdfsPaths, ",");
        String groupListStringyfied = convertListToString(hadoopAuthorizationGroups, ",");
        try 
        {
            sentryClientObject.createAcl(sentryConnection.getHadoopConfiguration(), groupListStringyfied,hdfsPathForACLCreation, HDFS_READ_ONLY_PERMISSION);
        } catch (Exception e) 
        {
            throw new RuntimeException("Failed to apply ACL in HDFS Kylo directories " + e.getMessage());
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
            if(sentryClientObject.checkIfRoleExists(sentryPolicyName)) {
                sentryClientObject.dropRole(sentryPolicyName);
            }


            sentryClientObject.createRole(sentryPolicyName);
            for( String groupCounter : groups)
            {
                sentryClientObject.grantRoleToGroup( sentryPolicyName, groupCounter);
            }
            for(String tableCounter : tableNames)
            {
                sentryClientObject.grantRolePriviledges( HIVE_READ_ONLY_PERMISSION, TABLE, datebaseName+"."+tableCounter, sentryPolicyName);

            }

        } catch (SentryClientException e) {
            throw new RuntimeException("Failed to create Sentry policy" + sentryPolicyName);
        }


    }

    @Override
    public void updateReadOnlyHdfsPolicy(String categoryName, String feedName, List<String> groups, List<String> hdfsPaths) {

        /**
         * Update Read Only Policy for HDFS - ACL Approach
         */

        String hdfsPathForACLCreation =  convertListToString(hdfsPaths, ",");
        String groupListStringyfied = convertListToString(groups, ",");

        try 
        {
            sentryClientObject.createAcl(sentryConnection.getHadoopConfiguration(), groupListStringyfied,hdfsPathForACLCreation, HDFS_READ_ONLY_PERMISSION);
        } catch (Exception e) 
        {
            throw new RuntimeException("Failed to apply ACL in HDFS Kylo directories " + e.getMessage());
        }

    }

    /**
     * If no security policy exists it will be created. If a policy exists it will be updated
     */
    @Override
    public void updateSecurityGroupsForAllPolicies(String categoryName, String feedName,List<String> securityGroupNames, Map<String,Object> feedProperties) {

        if (securityGroupNames == null || securityGroupNames.isEmpty()) {
            String hdfsFoldersWithCommas = ((String)feedProperties.get(REGISTRATION_HDFS_FOLDERS)).replace("\n", ",");
            List<String> hdfsFolders = Stream.of(hdfsFoldersWithCommas).collect(Collectors.toList());
            deleteHivePolicy(categoryName, feedName);
            deleteHdfsPolicy(categoryName, feedName,hdfsFolders);
        } else {

            String hdfsFoldersWithCommas = ((String)feedProperties.get(REGISTRATION_HDFS_FOLDERS)).replace("\n", ",");
            List<String> hdfsFolders = Stream.of(hdfsFoldersWithCommas).collect(Collectors.toList());
            createReadOnlyHdfsPolicy(categoryName, feedName, securityGroupNames, hdfsFolders);

            String sentryHivePolicyName = getHivePolicyName(categoryName, feedName);
            if(!sentryClientObject.checkIfRoleExists(sentryHivePolicyName)) {
                String hiveTablesWithCommas = ((String)feedProperties.get(REGISTRATION_HIVE_TABLES)).replace("\n", ",");
                List<String> hiveTables = Stream.of(hiveTablesWithCommas).collect(Collectors.toList());
                String hiveSchema = ((String)feedProperties.get(REGISTRATION_HIVE_SCHEMA));

                createOrUpdateReadOnlyHivePolicy(categoryName, feedName, securityGroupNames, hiveSchema, hiveTables);
            } else {
                try {
                    sentryClientObject.dropRole(sentryHivePolicyName);
                } catch (SentryClientException e) {
                    throw new RuntimeException("Unable to delete policy  "+ sentryHivePolicyName+" in Sentry  "  +e.getMessage());
                }
                String hiveTablesWithCommas = ((String)feedProperties.get(REGISTRATION_HIVE_TABLES)).replace("\n", ",");
                List<String> hiveTables = Stream.of(hiveTablesWithCommas).collect(Collectors.toList());
                String hiveSchema = ((String)feedProperties.get(REGISTRATION_HIVE_SCHEMA));
                List<String> hivePermissions = new ArrayList();
                hivePermissions.add(HIVE_READ_ONLY_PERMISSION);
                createOrUpdateReadOnlyHivePolicy(categoryName, feedName, securityGroupNames, hiveSchema, hiveTables);
            }
        }
    }


    @Override
    public void deleteHivePolicy(String categoryName, String feedName) {

        String sentryPolicyName = getHivePolicyName(categoryName, feedName);
        if(sentryClientObject.checkIfRoleExists(sentryPolicyName)) {
            try {
                sentryClientObject.dropRole(sentryPolicyName);
            } catch (SentryClientException e) {
                throw new RuntimeException("Unable to delete policy  "+ sentryPolicyName+" in Sentry  "  +e.getMessage());
            }
        }

    }

    @Override
    public void deleteHdfsPolicy(String categoryName, String feedName , List<String> hdfsPaths ) {

        /**
         * Delete ACL from list of HDFS Paths
         */

        String allPathForAclDeletion = convertListToString(hdfsPaths, ",");
        try {
            sentryClientObject.flushACL(sentryConnection.getHadoopConfiguration(), allPathForAclDeletion);
        } catch (IOException e) {
            throw new RuntimeException("Unable to remove ACL from HDFS Paths" +e.getMessage());
        }
    }



    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return HADOOP_AUTHORIZATION_TYPE_SENTRY;
    }

    private String getHivePolicyName(String categoryName, String feedName) {
        return KYLO_POLICY_PREFIX + "_" + categoryName + "_" + feedName + "_" + HIVE_REPOSITORY_TYPE;
    }

    /**
     * @return : comma separated string
     */
    private static String convertListToString(List<String> list, String delim) {

        StringBuilder sb = new StringBuilder();

        String loopDelim = "";

        for (String input : list) {

            sb.append(loopDelim);
            sb.append(input);

            loopDelim = delim;
        }
        return sb.toString();
    }
}
