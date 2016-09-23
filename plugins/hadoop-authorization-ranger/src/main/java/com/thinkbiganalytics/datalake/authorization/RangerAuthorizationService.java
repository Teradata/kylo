package com.thinkbiganalytics.datalake.authorization;

import com.thinkbiganalytics.datalake.authorization.config.RangerConnection;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;
import com.thinkbiganalytics.datalake.authorization.rest.client.RangerRestClient;
import com.thinkbiganalytics.datalake.authorization.rest.client.RangerRestClientConfig;
import com.thinkbiganalytics.datalake.authorization.rest.model.RangerGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RangerAuthorizationService implements HadoopAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(RangerAuthorizationService.class);

    private RangerRestClient rangerRestClient;


    /**
     * Implement Ranger Authentication Service. Initiate RangerClient and RangerClientConfig for initializing service and invoke different methods of it.
     */

    @Override
    public void initialize(AuthorizationConfiguration config) {
        RangerConnection rangerConnHelper = (RangerConnection) config;
        RangerRestClientConfig rangerClientConfiguration = new RangerRestClientConfig(rangerConnHelper.getHostName(), rangerConnHelper.getUsername(), rangerConnHelper.getPassword());
        rangerClientConfiguration.setPort(rangerConnHelper.getPort());

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

}
