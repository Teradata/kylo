package com.thinkbiganalytics.datalake.authorization;

/*-
 * #%L
 * thinkbig-hadoop-authorization-ranger
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

import com.thinkbiganalytics.datalake.authorization.config.AuthorizationConfiguration;
import com.thinkbiganalytics.datalake.authorization.config.RangerConnection;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;
import com.thinkbiganalytics.datalake.authorization.rest.client.RangerRestClient;
import com.thinkbiganalytics.datalake.authorization.rest.client.RangerRestClientConfig;
import com.thinkbiganalytics.datalake.authorization.rest.model.RangerCreateOrUpdatePolicy;
import com.thinkbiganalytics.datalake.authorization.rest.model.RangerGroup;
import com.thinkbiganalytics.datalake.authorization.rest.model.RangerPolicy;
import com.thinkbiganalytics.datalake.authorization.service.BaseHadoopAuthorizationService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RangerAuthorizationService extends BaseHadoopAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(RangerAuthorizationService.class);

    private static final String HADOOP_AUTHORIZATION_TYPE_RANGER = "RANGER";
    private static final String HDFS_REPOSITORY_TYPE = "hdfs";
    private static final String HIVE_REPOSITORY_TYPE = "hive";
    private static final String IsEnable = "true";
    private static final String IsRecursive = "true";
    private static final String IsAuditable = "true";
    private static final String REPOSITORY_TYPE = "repositoryType";
    private static final String POLICY_NAME = "policyName";
    private static final String HIVE_COLUMN_PERMISSION = "*";
    private static final String HDFS_READ_ONLY_PERMISSION = "read";
    private static final String HIVE_READ_ONLY_PERMISSION = "select";
    private static final String KYLO_POLICY_PREFIX = "kylo";

    private RangerRestClient rangerRestClient;
    private RangerConnection rangerConnection;

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

    /**
     * Implement Ranger Authentication Service. Initiate RangerClient and RangerClientConfig for initializing service and invoke different methods of it.
     */
    @Override
    public void initialize(AuthorizationConfiguration config) {
        rangerConnection = (RangerConnection) config;
        RangerRestClientConfig rangerClientConfiguration = new RangerRestClientConfig(rangerConnection.getHostName(), rangerConnection.getUsername(), rangerConnection.getPassword());
        rangerClientConfiguration.setPort(rangerConnection.getPort());
        rangerRestClient = new RangerRestClient(rangerClientConfiguration);
    }

    @Override
    public RangerGroup getGroupByName(String groupName) {
        return rangerRestClient.getGroupByName(groupName);
    }

    @Override
    public List<HadoopAuthorizationGroup> getAllGroups() {
        return rangerRestClient.getAllGroups();
    }

    @Override
    public void createOrUpdateReadOnlyHivePolicy(String categoryName, String feedName, List<String> securityGroupNames, String datebaseName, List<String> tableNames) {
        String rangerHivePolicyName = getHivePolicyName(categoryName, feedName);

        Map<String, Object> searchHiveCriteria = new HashMap<>();
        searchHiveCriteria.put(POLICY_NAME, rangerHivePolicyName);
        searchHiveCriteria.put(REPOSITORY_TYPE, HIVE_REPOSITORY_TYPE);
        List<RangerPolicy> hadoopPolicyList = this.searchPolicy(searchHiveCriteria);

        if (hadoopPolicyList.size() > 1) {
            throw new RuntimeException("More than 1 unique Hive policy was found for " + rangerHivePolicyName);
        } else if (hadoopPolicyList.size() == 0) {
            createReadOnlyHivePolicy(categoryName, feedName, securityGroupNames, datebaseName, tableNames);
        } else {
            updateReadOnlyHivePolicy(categoryName, feedName, securityGroupNames, datebaseName, tableNames);
        }
    }

    @Override
    public void createOrUpdateReadOnlyHdfsPolicy(String categoryName, String feedName, List<String> securityGroupNames, List<String> hdfsPaths) {
        String rangerHdfsPolicyName = getHdfsPolicyName(categoryName, feedName);

        Map<String, Object> searchHDFSCriteria = new HashMap<>();
        searchHDFSCriteria.put(POLICY_NAME, rangerHdfsPolicyName);
        searchHDFSCriteria.put(REPOSITORY_TYPE, HDFS_REPOSITORY_TYPE);
        List<RangerPolicy> hadoopPolicyList = this.searchPolicy(searchHDFSCriteria);

        if (hadoopPolicyList.size() > 1) {
            throw new RuntimeException("More than 1 unique HDFS policy was found for " + rangerHdfsPolicyName);
        } else if (hadoopPolicyList.size() == 0) {
            createReadOnlyHdfsPolicy(categoryName, feedName, securityGroupNames, hdfsPaths);
        } else {
            updateReadOnlyHdfsPolicy(categoryName, feedName, securityGroupNames, hdfsPaths);
        }
    }

    @Override
    public void createReadOnlyHivePolicy(String categoryName, String feedName, List<String> securityGroupNames, String databaseName, List<String> tableNames) {

        RangerCreateOrUpdatePolicy rangerCreateOrUpdatePolicy = new RangerCreateOrUpdatePolicy();

        List<String> hivePermissions = new ArrayList<>();
        hivePermissions.add(HIVE_READ_ONLY_PERMISSION);
        String rangerHivePolicyName = getHivePolicyName(categoryName, feedName);
        String hiveDescription = "Ranger policy created for group list " + securityGroupNames.toString() + " for resource " + tableNames.toString();
        String hiveTables = convertListToString(tableNames, ",");

        rangerCreateOrUpdatePolicy = new RangerCreateOrUpdatePolicy();

        rangerCreateOrUpdatePolicy.setPolicyName(rangerHivePolicyName);
        rangerCreateOrUpdatePolicy.setDatabases(databaseName);
        rangerCreateOrUpdatePolicy.setTables(hiveTables);
        rangerCreateOrUpdatePolicy.setColumns(HIVE_COLUMN_PERMISSION);
        rangerCreateOrUpdatePolicy.setUdfs("");
        rangerCreateOrUpdatePolicy.setDescription(hiveDescription);
        rangerCreateOrUpdatePolicy.setRepositoryName(rangerConnection.getHiveRepositoryName());
        rangerCreateOrUpdatePolicy.setRepositoryType(HIVE_REPOSITORY_TYPE);
        rangerCreateOrUpdatePolicy.setIsAuditEnabled(IsAuditable);
        rangerCreateOrUpdatePolicy.setIsEnabled(IsEnable);
        rangerCreateOrUpdatePolicy.setPermMapList(securityGroupNames, hivePermissions);

        try {
            rangerRestClient.createPolicy(rangerCreateOrUpdatePolicy);
        } catch (Exception e) {
            log.error("Error creating Hive Ranger policy", e);
            throw new RuntimeException("Error creating Hive Ranger policy", e);
        }

    }

    @Override
    public void createReadOnlyHdfsPolicy(String categoryName, String feedName, List<String> securityGroupNames, List<String> hdfsPaths) {

        RangerCreateOrUpdatePolicy rangerCreateOrUpdatePolicy = new RangerCreateOrUpdatePolicy();

        /**
         * Create HDFS Policy
         */
        List<String> hdfsPermissions = new ArrayList<>();
        hdfsPermissions.add(HDFS_READ_ONLY_PERMISSION);
        String rangerHdfsPolicyName = getHdfsPolicyName(categoryName, feedName);
        String description = "Ranger policy created for group list " + securityGroupNames.toString() + " for resource " + hdfsPaths.toString();
        String hdfsResource = convertListToString(hdfsPaths, ",");

        rangerCreateOrUpdatePolicy.setPolicyName(rangerHdfsPolicyName);
        rangerCreateOrUpdatePolicy.setResourceName(hdfsResource);
        rangerCreateOrUpdatePolicy.setDescription(description);
        rangerCreateOrUpdatePolicy.setRepositoryName(rangerConnection.getHdfsRepositoryName());
        rangerCreateOrUpdatePolicy.setRepositoryType(HDFS_REPOSITORY_TYPE);
        rangerCreateOrUpdatePolicy.setIsEnabled(IsEnable);
        rangerCreateOrUpdatePolicy.setIsRecursive(IsRecursive);
        rangerCreateOrUpdatePolicy.setIsAuditEnabled(IsAuditable);
        rangerCreateOrUpdatePolicy.setPermMapList(securityGroupNames, hdfsPermissions);

        try {
            rangerRestClient.createPolicy(rangerCreateOrUpdatePolicy);
        } catch (Exception e) {
            log.error("Error creating HDFS Ranger policy", e);
            throw new RuntimeException("Error creating HDFS Ranger policy", e);
        }
    }

    @Override
    public void updateReadOnlyHivePolicy(String categoryName, String feedName, List<String> groups, String datebaseNames, List<String> tableNames) {

        int policyId;

        String rangerHivePolicyName = getHivePolicyName(categoryName, feedName);
        Map<String, Object> searchHiveCriteria = new HashMap<>();
        searchHiveCriteria.put(POLICY_NAME, rangerHivePolicyName);
        searchHiveCriteria.put(REPOSITORY_TYPE, HIVE_REPOSITORY_TYPE);
        List<RangerPolicy> hadoopPolicyList = this.searchPolicy(searchHiveCriteria);

        policyId = 0;
        if (hadoopPolicyList.size() == 0) {
            throw new UnsupportedOperationException("Ranger Plugin : Unable to get ID for Ranger Hive Policy");
        } else {
            if (hadoopPolicyList.size() > 1) {
                throw new RuntimeException("Unable to find Hive unique policy.");
            } else {

                for (RangerPolicy hadoopPolicy : hadoopPolicyList) {
                    policyId = hadoopPolicy.getId();
                }
            }
        }

        List<String> hivePermissions = new ArrayList<>();
        hivePermissions.add(HIVE_READ_ONLY_PERMISSION);
        String hiveDescription = "Ranger policy updated for group list " + groups.toString() + " for resource " + datebaseNames;
        String hiveTables = convertListToString(tableNames, ",");

        RangerCreateOrUpdatePolicy rangerUpdatePolicy = new RangerCreateOrUpdatePolicy();
        rangerUpdatePolicy.setPolicyName(rangerHivePolicyName);
        rangerUpdatePolicy.setDatabases(datebaseNames);
        rangerUpdatePolicy.setTables(hiveTables);
        rangerUpdatePolicy.setColumns(HIVE_COLUMN_PERMISSION);
        rangerUpdatePolicy.setUdfs("");
        rangerUpdatePolicy.setDescription(hiveDescription);
        rangerUpdatePolicy.setRepositoryName(rangerConnection.getHiveRepositoryName());
        rangerUpdatePolicy.setRepositoryType(HIVE_REPOSITORY_TYPE);
        rangerUpdatePolicy.setIsAuditEnabled(IsAuditable);
        rangerUpdatePolicy.setIsEnabled(IsEnable);
        rangerUpdatePolicy.setPermMapList(groups, hivePermissions);

        try {
            rangerRestClient.updatePolicy(rangerUpdatePolicy, policyId);
        } catch (Exception e) {
            log.error("Failed to update Hive Policy", e);
            throw new RuntimeException("Failed to update Hive Policy", e);
        }

    }

    @Override
    public void updateReadOnlyHdfsPolicy(String categoryName, String feedName, List<String> groups, List<String> hdfsPaths) {

        int policyId = 0;
        String rangerHdfsPolicyName = getHdfsPolicyName(categoryName, feedName);

        Map<String, Object> searchHDFSCriteria = new HashMap<>();
        searchHDFSCriteria.put(POLICY_NAME, rangerHdfsPolicyName);
        searchHDFSCriteria.put(REPOSITORY_TYPE, HDFS_REPOSITORY_TYPE);
        List<RangerPolicy> hadoopPolicyList = this.searchPolicy(searchHDFSCriteria);

        if (hadoopPolicyList.size() == 0) {
            throw new UnsupportedOperationException("Ranger Plugin : Unable to get ID for Ranger HDFS Policy");
        } else {
            if (hadoopPolicyList.size() > 1) {
                throw new RuntimeException("Unable to find HDFS unique policy.");
            } else {

                for (RangerPolicy hadoopPolicy : hadoopPolicyList) {
                    policyId = hadoopPolicy.getId();
                }
            }
        }

        RangerCreateOrUpdatePolicy rangerUpdatePolicy = new RangerCreateOrUpdatePolicy();

        /**
         * Update HDFS Policy
         */
        List<String> hdfsPermissions = new ArrayList<>();
        hdfsPermissions.add(HDFS_READ_ONLY_PERMISSION);

        String description = "Ranger policy updated for group list " + groups.toString() + " for resource " + hdfsPaths.toString();
        String hdfs_resource = convertListToString(hdfsPaths, ",");

        rangerUpdatePolicy.setPolicyName(rangerHdfsPolicyName);
        rangerUpdatePolicy.setResourceName(hdfs_resource);
        rangerUpdatePolicy.setDescription(description);
        rangerUpdatePolicy.setRepositoryName(rangerConnection.getHdfsRepositoryName());
        rangerUpdatePolicy.setRepositoryType(HDFS_REPOSITORY_TYPE);
        rangerUpdatePolicy.setIsEnabled(IsEnable);
        rangerUpdatePolicy.setIsRecursive(IsRecursive);
        rangerUpdatePolicy.setIsAuditEnabled(IsAuditable);
        rangerUpdatePolicy.setPermMapList(groups, hdfsPermissions);

        try {
            rangerRestClient.updatePolicy(rangerUpdatePolicy, policyId);
        } catch (Exception e) {
            log.error("Failed to update HDFS policy", e);
            throw new RuntimeException("Failed to update HDFS policy", e);
        }
    }

    @Override
    /**
     * If no security policy exists it will be created. If a policy exists it will be updated
     */
    public void updateSecurityGroupsForAllPolicies(String categoryName, String feedName, List<String> securityGroupNames, Map<String, Object> feedProperties) {
        String rangerHdfsPolicyName = getHdfsPolicyName(categoryName, feedName);
        RangerPolicy hdfsPolicy = searchHdfsPolicy(rangerHdfsPolicyName);
        String rangerHivePolicyName = getHivePolicyName(categoryName, feedName);
        RangerPolicy hivePolicy = searchHivePolicy(rangerHivePolicyName);

        if (securityGroupNames == null || securityGroupNames.isEmpty()) {
            // Only delete if the policies exists. It's possibile that someone adds a security group right after feed creation and before initial ingestion
            if (hivePolicy != null) {
                deleteHivePolicy(categoryName, feedName);
            }
            if (hdfsPolicy != null) {
                deleteHdfsPolicy(categoryName, feedName, null);
            }
        } else {
            if (hdfsPolicy == null) {
                // If a feed hasn't run yet the metadata won't exists
                if (!StringUtils.isEmpty((String) feedProperties.get(REGISTRATION_HDFS_FOLDERS))) {
                    String hdfsFoldersWithCommas = ((String) feedProperties.get(REGISTRATION_HDFS_FOLDERS)).replace("\n", ",");
                    List<String> hdfsFolders = Stream.of(hdfsFoldersWithCommas).collect(Collectors.toList());
                    createReadOnlyHdfsPolicy(categoryName, feedName, securityGroupNames, hdfsFolders);
                }
            } else {
                List<String> hdfsPermissions = new ArrayList<>();
                hdfsPermissions.add(HDFS_READ_ONLY_PERMISSION);

                RangerCreateOrUpdatePolicy updatePolicy = new RangerCreateOrUpdatePolicy();
                BeanUtils.copyProperties(hdfsPolicy, updatePolicy);
                updatePolicy.setPermMapList(securityGroupNames, hdfsPermissions);

                rangerRestClient.updatePolicy(updatePolicy, hdfsPolicy.getId());
            }

            if (hivePolicy == null) {
                // If a feed hasn't run yet the metadata won't exists
                if (!StringUtils.isEmpty((String) feedProperties.get(REGISTRATION_HIVE_TABLES))) {
                    String hiveTablesWithCommas = ((String) feedProperties.get(REGISTRATION_HIVE_TABLES)).replace("\n", ",");
                    List<String> hiveTables = Stream.of(hiveTablesWithCommas).collect(Collectors.toList());
                    String hiveSchema = ((String) feedProperties.get(REGISTRATION_HIVE_SCHEMA));
                    createOrUpdateReadOnlyHivePolicy(categoryName, feedName, securityGroupNames, hiveSchema, hiveTables);
                }
            } else {
                List<String> hivePermissions = new ArrayList<>();
                hivePermissions.add(HIVE_READ_ONLY_PERMISSION);
                RangerCreateOrUpdatePolicy updatePolicy = new RangerCreateOrUpdatePolicy();
                BeanUtils.copyProperties(hivePolicy, updatePolicy);
                updatePolicy.setPermMapList(securityGroupNames, hivePermissions);
                rangerRestClient.updatePolicy(updatePolicy, hivePolicy.getId());
            }
        }
    }

    private String getHdfsPolicyName(String categoryName, String feedName) {
        return KYLO_POLICY_PREFIX + "_" + categoryName + "_" + feedName + "_" + HDFS_REPOSITORY_TYPE;
    }

    private String getHivePolicyName(String categoryName, String feedName) {
        return KYLO_POLICY_PREFIX + "_" + categoryName + "_" + feedName + "_" + HIVE_REPOSITORY_TYPE;
    }

    private RangerPolicy searchHdfsPolicy(String hdfsPolicyName) {
        RangerPolicy result = null;

        Map<String, Object> searchHDFSCriteria = new HashMap<>();
        searchHDFSCriteria.put(POLICY_NAME, hdfsPolicyName);
        searchHDFSCriteria.put(REPOSITORY_TYPE, HDFS_REPOSITORY_TYPE);
        List<RangerPolicy> hadoopPolicyList = this.searchPolicy(searchHDFSCriteria);
        if (hadoopPolicyList.size() > 1) {
            throw new RuntimeException("More than 1 unique HDFS policy was found for " + hdfsPolicyName);
        } else if (hadoopPolicyList != null && !hadoopPolicyList.isEmpty()) {
            result = hadoopPolicyList.get(0);
        }
        return result;
    }

    private RangerPolicy searchHivePolicy(String hivePolicyName) {
        RangerPolicy result = null;

        Map<String, Object> searchHiveCriteria = new HashMap<>();
        searchHiveCriteria.put(POLICY_NAME, hivePolicyName);
        searchHiveCriteria.put(REPOSITORY_TYPE, HIVE_REPOSITORY_TYPE);
        List<RangerPolicy> hadoopPolicyList = this.searchPolicy(searchHiveCriteria);
        if (hadoopPolicyList.size() > 1) {
            throw new RuntimeException("More than 1 unique Hive policy was found for " + hivePolicyName);
        } else if (hadoopPolicyList != null && !hadoopPolicyList.isEmpty()) {
            result = hadoopPolicyList.get(0);
        }
        return result;


    }

    private List<RangerPolicy> searchPolicy(Map<String, Object> searchCriteria) {
        return rangerRestClient.searchPolicies(searchCriteria);
    }

    @Override
    public void deleteHivePolicy(String categoryName, String feedName) {
        String rangerHivePolicyName = getHivePolicyName(categoryName, feedName);

        Map<String, Object> searchHiveCriteria = new HashMap<>();
        searchHiveCriteria.put(POLICY_NAME, rangerHivePolicyName);
        searchHiveCriteria.put(REPOSITORY_TYPE, HIVE_REPOSITORY_TYPE);
        List<RangerPolicy> hadoopPolicyList = this.searchPolicy(searchHiveCriteria);

        if (hadoopPolicyList.size() == 0) {
            log.info("Ranger Plugin : Unable to find Ranger Hive policy " + rangerHivePolicyName + " ... Ignoring");
        } else if (hadoopPolicyList.size() > 1) {
            throw new RuntimeException("Unable to find Hive unique policy.");
        } else {

            for (RangerPolicy hadoopPolicy : hadoopPolicyList) {
                try {
                    rangerRestClient.deletePolicy(hadoopPolicy.getId());
                } catch (Exception e) {
                    log.error("Unable to delete policy", e);
                    throw new RuntimeException("Unable to delete policy for " + rangerHivePolicyName, e);
                }

            }
        }

    }

    @Override
    public void deleteHdfsPolicy(String categoryName, String feedName, List<String> hdfsPaths) {

        String rangerHdfsPolicyName = getHdfsPolicyName(categoryName, feedName);

        Map<String, Object> searchHdfsCriteria = new HashMap<>();
        searchHdfsCriteria.put(POLICY_NAME, rangerHdfsPolicyName);
        searchHdfsCriteria.put(REPOSITORY_TYPE, HDFS_REPOSITORY_TYPE);
        List<RangerPolicy> hadoopPolicyList = this.searchPolicy(searchHdfsCriteria);

        if (hadoopPolicyList.size() == 0) {
            log.info("Ranger Plugin : Unable to find Ranger HDFS policy " + rangerHdfsPolicyName + " ... Ignoring");
        } else if (hadoopPolicyList.size() > 1) {
            throw new RuntimeException("Unable to find HDFS unique policy.");
        } else {
            for (RangerPolicy hadoopPolicy : hadoopPolicyList) {
                try {
                    rangerRestClient.deletePolicy(hadoopPolicy.getId());
                } catch (Exception e) {
                    log.error("Unable to delete policy", e);
                    throw new RuntimeException("Unable to delete policy for " + rangerHdfsPolicyName, e);
                }
            }
        }
    }

    @Override
    public String getType() {
        return HADOOP_AUTHORIZATION_TYPE_RANGER;
    }

}
