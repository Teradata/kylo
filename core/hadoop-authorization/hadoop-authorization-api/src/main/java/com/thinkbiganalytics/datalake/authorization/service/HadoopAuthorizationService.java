package com.thinkbiganalytics.datalake.authorization.service;

import com.thinkbiganalytics.datalake.authorization.config.AuthorizationConfiguration;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Shashi Vishwakarma on 9/9/16.
 */

public interface HadoopAuthorizationService {

    static final String HADOOP_AUTHORIZATION_TYPE_NONE = "NONE";
    public static final String REGISTRATION_HDFS_FOLDERS = "nifi:registration:hdfsFolders";
    public static final String REGISTRATION_HIVE_SCHEMA = "nifi:registration:hiveSchema";
    public static final String REGISTRATION_HIVE_TABLES = "nifi:registration:hiveTableNames";

    void initialize(AuthorizationConfiguration configuration);

    HadoopAuthorizationGroup getGroupByName(String groupName);

    List<HadoopAuthorizationGroup> getAllGroups();

    void createOrUpdateReadOnlyHivePolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, String datebaseName, List<String> tableNames);

    void createOrUpdateReadOnlyHdfsPolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, List<String> hdfsPaths);

    void createReadOnlyHivePolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, String datebaseName, List<String> tableNames);

    void createReadOnlyHdfsPolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, List<String> hdfsPaths);

    void deleteHivePolicy(String categoryName, String feedName);

    void deleteHdfsPolicy(String categoryName, String feedName , List<String> hdfsPaths);

    void updateReadOnlyHivePolicy(String categoryName, String feedName, List<String> groups, String datebaseName, List<String> tableNames);

    void updateReadOnlyHdfsPolicy(String categoryName, String feedName, List<String> groups, List<String> hdfsPaths);

    void updateSecurityGroupsForAllPolicies(String categoryName, String feedName,List<String> hadoopAuthorizationGroups, Map<String,Object> feedProperties);

    String getType();

    public static List<String> convertNewlineDelimetedTextToList(String text) {
        List<String> result = new ArrayList<>();
        if(StringUtils.isNotEmpty(text)) {
            String listWithCommas = text.replace("\n", ",");
            result = Arrays.asList(listWithCommas.split(",")).stream().collect(Collectors.toList());
        }
        return result;
    }

}
