package com.thinkbiganalytics.feedmgr.nifi;


import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;

/**
 * Created by sr186054 on 1/14/16.
 */
public class NifiRestService {

    public NifiRestClient nifiRestClient;

    public NifiRestService(NifiRestClientConfig restClientConfig){
           nifiRestClient = new NifiRestClient(restClientConfig);
    }

    public NifiRestClient getNifiRestClient() {
        return nifiRestClient;
    }

}
