package com.thinkbiganalytics.datalake.authorization;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.datalake.authorization.client.SentryClient;
import com.thinkbiganalytics.datalake.authorization.client.SentryClientConfig;
import com.thinkbiganalytics.datalake.authorization.config.SentryConnection;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationPolicy;

/**
 * Sentry Authorization Service
 *
 * Created by Shashi Vishwakarma on 19/9/2016.
 *
 */
public class SentryAuthorizationService implements HadoopAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(SentryAuthorizationService.class);
    
    SentryClient sentryClientObject;

    @Override
    public void initialize(AuthorizationConfiguration config) 
    {
    	
    	 SentryConnection sentryConnHelper = (SentryConnection) config;
         SentryClientConfig sentryClientConfiguration = new SentryClientConfig(sentryConnHelper.getDriverName() , sentryConnHelper.getConnectionURL() , sentryConnHelper.getUsername() , sentryConnHelper.getPassword());
         sentryClientObject = new SentryClient(sentryClientConfiguration);
         
    }

    @Override
    public HadoopAuthorizationGroup getGroupByName(String groupName) {
        return null;
    }

    @Override
    public List<HadoopAuthorizationGroup> getAllGroups() {
        return null;
    }

	@Override
	public void createPolicy(String policyName, List<String> group_List, List<String> hdfs_paths,
			String permission_level, List<String> datebaseName, List<String> tableName,
			List<String> hdfs_permission_list, List<String> hive_permission_List) {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public void deletePolicy(String policyName , String repositoyType) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<HadoopAuthorizationPolicy> searchPolicy(Map<String, Object> searchCriteria) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updatePolicy(String policyName, List<String> group_List, List<String> hdfs_paths,
			String permission_level, List<String> datebaseName, List<String> tableName,
			List<String> hdfs_permission_list, List<String> hive_permission_List) {
		// TODO Auto-generated method stub
		
	}

}
