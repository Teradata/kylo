package com.thinkbiganalytics.datalake.authorization;

import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;

import java.util.List;

/***
 * @author sv186029
 */

public interface HadoopAuthorizationService {
    //void initiateAuthorizationService() throws Exception;

    void initialize(AuthorizationConfiguration configuration);

    HadoopAuthorizationGroup getGroupByName(String groupName);

    List<HadoopAuthorizationGroup> getAllGroups();

}
