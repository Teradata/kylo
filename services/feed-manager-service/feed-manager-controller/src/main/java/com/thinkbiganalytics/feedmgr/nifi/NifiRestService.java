package com.thinkbiganalytics.feedmgr.nifi;


import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;

/**
 * Created by sr186054 on 1/14/16.
 */
public class NifiRestService {

    public LegacyNifiRestClient nifiRestClient;

    public NifiRestService(NifiRestClientConfig restClientConfig){
           nifiRestClient = new LegacyNifiRestClient();
    }

    public LegacyNifiRestClient getNifiRestClient() {
        return nifiRestClient;
    }

}
