package com.thinkbiganalytics.datalake.authorization;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.datalake.authorization.client.SentryClient;
import com.thinkbiganalytics.datalake.authorization.client.SentryClientConfig;
import com.thinkbiganalytics.datalake.authorization.client.SentryClientException;
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

    private static final String HADOOP_AUTHORIZATION_TYPE_SENTRY = "SENTRY";

    SentryClient sentryClientObject;

    @Override
    public void initialize(AuthorizationConfiguration config) 
    {

        SentryConnection sentryConnection = (SentryConnection) config;
        // SentryClientConfig sentryClientConfiguration = new SentryClientConfig(sentryConnHelper.getDriverName() , sentryConnHelper.getConnectionURL() , sentryConnHelper.getUsername() , sentryConnHelper.getPassword());
        SentryClientConfig sentryClientConfiguration = new SentryClientConfig(sentryConnection.getDataSource()); 
        sentryClientObject = new SentryClient(sentryClientConfiguration);
    }

    @Override
    public HadoopAuthorizationGroup getGroupByName(String groupName) {
        return null;
    }

    @Override
    public List<HadoopAuthorizationGroup> getAllGroups() {
        try {
            sentryClientObject.createRole("thiSiShashi");
        } catch (SentryClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    

    @Override
    public void createReadOnlyPolicy(String categoryName, String feedName, List<String> hadoopAuthorizationGroups, List<String> hdfsPaths, String datebaseName, List<String> tableNames) {

        /**
         * Create Read Only Policy for Hive - Beeline Approach
         */


        /**
         * Create Read Only Policy for HDFS - ACL Approach
         */

    }

    @Override
    public void deletePolicy(String categoryName, String feedName, String repositoryType) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public List<HadoopAuthorizationPolicy> searchPolicy(Map<String, Object> searchCriteria) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateReadOnlyPolicy(String categoryName, String feedName, List<String> group_List, List<String> hdfs_paths, String datebaseName, List<String> tableName) throws Exception {
        // TODO Auto-generated method stub

        /**
         * Update Read Only Policy for Hive - Beeline Approach
         */


        /**
         * Update Read Only Policy for HDFS - ACL Approach
         */

    }

    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return HADOOP_AUTHORIZATION_TYPE_SENTRY;
    }

}
