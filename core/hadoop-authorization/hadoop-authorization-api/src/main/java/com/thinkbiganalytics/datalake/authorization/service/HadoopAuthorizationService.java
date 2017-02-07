package com.thinkbiganalytics.datalake.authorization.service;

/*-
 * #%L
 * thinkbig-hadoop-authorization-api
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
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 */

public interface HadoopAuthorizationService {

    static final String HADOOP_AUTHORIZATION_TYPE_NONE = "NONE";
    public static final String REGISTRATION_HDFS_FOLDERS = "nifi:registration:hdfsFolders";
    public static final String REGISTRATION_HIVE_SCHEMA = "nifi:registration:hiveSchema";
    public static final String REGISTRATION_HIVE_TABLES = "nifi:registration:hiveTableNames";

    public static List<String> convertNewlineDelimetedTextToList(String text) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isNotEmpty(text)) {
            String listWithCommas = text.replace("\n", ",");
            result = Arrays.asList(listWithCommas.split(",")).stream().collect(Collectors.toList());
        }
        return result;
    }

    void initialize(AuthorizationConfiguration configuration);

    HadoopAuthorizationGroup getGroupByName(String groupName);

    List<HadoopAuthorizationGroup> getAllGroups();

    void createOrUpdateReadOnlyHivePolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, String datebaseName, List<String> tableNames);

    void createOrUpdateReadOnlyHdfsPolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, List<String> hdfsPaths);

    void createReadOnlyHivePolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, String datebaseName, List<String> tableNames);

    void createReadOnlyHdfsPolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, List<String> hdfsPaths);

    void deleteHivePolicy(String categoryName, String feedName);

    void deleteHdfsPolicy(String categoryName, String feedName, List<String> hdfsPaths);

    void updateReadOnlyHivePolicy(String categoryName, String feedName, List<String> groups, String datebaseName, List<String> tableNames);

    void updateReadOnlyHdfsPolicy(String categoryName, String feedName, List<String> groups, List<String> hdfsPaths);

    void updateSecurityGroupsForAllPolicies(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, Map<String, Object> feedProperties);

    String getType();

}
