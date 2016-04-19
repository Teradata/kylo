package com.thinkbiganalytics.feedmgr.service;

import com.thinkbiganalytics.hive.rest.HiveRestClient;
import com.thinkbiganalytics.hive.rest.HiveRestClientConfig;

/**
 * Created by sr186054 on 2/12/16.
 */
//TODO move Configuration to properties file
public class HiveService {

    public HiveRestClient restClient;

    private static class LazyHolder {
        static final HiveService INSTANCE = new HiveService();
    }

    public static HiveService getInstance() {
        return LazyHolder.INSTANCE;
    }

    private HiveService() {
        setupRestClient();
    }


    public void setupRestClient(){
        HiveRestClientConfig config = new HiveRestClientConfig();
        config.setHost("localhost");
        config.setPort(8282);
        restClient = new HiveRestClient(config);
    }

    public HiveRestClient getHiveRestClient() {
        return restClient;
    }

}