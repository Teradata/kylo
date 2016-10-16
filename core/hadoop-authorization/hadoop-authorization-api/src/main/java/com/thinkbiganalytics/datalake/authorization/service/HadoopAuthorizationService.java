package com.thinkbiganalytics.datalake.authorization.service;

import com.thinkbiganalytics.datalake.authorization.config.AuthorizationConfiguration;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationPolicy;

import java.util.List;
import java.util.Map;

/**
 * Created by Shashi Vishwakarma on 9/9/16.
 */

public interface HadoopAuthorizationService {

    static final String HADOOP_AUTHORIZATION_TYPE_NONE = "NONE";

    void initialize(AuthorizationConfiguration configuration);

    HadoopAuthorizationGroup getGroupByName(String groupName);

    List<HadoopAuthorizationGroup> getAllGroups();

    void createOrUpdateReadOnlyHivePolicy(String categoryName, String feedName , List<String> hadoopAuthorizationGroups, String datebaseName, List<String> tableNames);

    void createOrUpdateReadOnlyHdfsPolicy(String categoryName, String feedName , List<String> hadoopAuthorizationGroups, List<String> hdfsPaths);

    void createReadOnlyHivePolicy(String categoryName, String feedName , List<String> hadoopAuthorizationGroups, String datebaseName, List<String> tableNames);

    void createReadOnlyHdfsPolicy(String categoryName, String feedName , List<String> hadoopAuthorizationGroups, List<String> hdfsPaths);

    void deletePolicy(String categoryName, String feedName , String repositoryType) throws Exception;

    List<HadoopAuthorizationPolicy> searchPolicy(Map<String, Object> searchCriteria);

    void updateReadOnlyHivePolicy(String categoryName, String feedName , List<String> groups, String datebaseName, List<String> tableNames) throws Exception;

    void updateReadOnlyHdfsPolicy(String categoryName, String feedName , List<String> groups, List<String> hdfsPaths) throws Exception;

    String getType();

}
