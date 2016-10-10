package com.thinkbiganalytics.datalake.authorization;

import java.util.List;
import java.util.Map;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationPolicy;

/**
 * Created by Shashi Vishwakarma on 9/9/16.
 */

public interface HadoopAuthorizationService {
    //void initiateAuthorizationService() throws Exception;

    void initialize(AuthorizationConfiguration configuration);

    HadoopAuthorizationGroup getGroupByName(String groupName);

    List<HadoopAuthorizationGroup> getAllGroups();
    
    void createPolicy(String policyName , List<String> group_List ,List<String> hdfs_paths, String permission_level , List<String> datebaseName, List<String>  tableName,  List<String>  hdfs_permission_list , List<String>  hive_permission_List );
    
    void deletePolicy(String policyName , String repositoryType) throws Exception;
    
    List<HadoopAuthorizationPolicy> searchPolicy(Map<String, Object> searchCriteria);

	void updatePolicy(String policyName , List<String> group_List ,List<String> hdfs_paths, String permission_level , List<String> datebaseName, List<String>  tableName,  List<String>  hdfs_permission_list , List<String>  hive_permission_List ) throws Exception;

}
