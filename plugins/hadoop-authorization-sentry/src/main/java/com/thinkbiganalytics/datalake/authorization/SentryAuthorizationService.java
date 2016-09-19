package com.thinkbiganalytics.datalake.authorization;

import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Sentry Authorization Service
 *
 * @author sv186029
 */
public class SentryAuthorizationService implements HadoopAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(SentryAuthorizationService.class);

    @Override
    public void initialize(AuthorizationConfiguration config) {
        throw new RuntimeException(("Implement me"));

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
