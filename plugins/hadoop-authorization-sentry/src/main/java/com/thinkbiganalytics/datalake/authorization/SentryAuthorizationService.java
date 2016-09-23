package com.thinkbiganalytics.datalake.authorization;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.datalake.authorization.client.SentryClient;
import com.thinkbiganalytics.datalake.authorization.client.SentryClientConfig;
import com.thinkbiganalytics.datalake.authorization.config.SentryConnection;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;

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

}
