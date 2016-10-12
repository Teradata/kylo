package com.thinkbiganalytics.datalake.authorization;

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

    void createReadOnlyPolicy(String categoryName, String feedName , List<String> hadoopAuthorizationGroups, List<String> hdfsPaths,
                              String datebaseName, List<String> tableNames);

    void deletePolicy(String categoryName, String feedName , String repositoryType) throws Exception;

    List<HadoopAuthorizationPolicy> searchPolicy(Map<String, Object> searchCriteria);

    void updateReadOnlyPolicy(String categoryName, String feedName , List<String> group_List, List<String> hdfs_paths,
                              String datebaseName, List<String> tableName) throws Exception;

    String getType();

}
