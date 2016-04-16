package com.thinkbiganalytics.servicemonitor.rest.client.cdh;

import com.cloudera.api.ApiRootResource;
import com.cloudera.api.ClouderaManagerClientBuilder;
import com.thinkbiganalytics.servicemonitor.rest.client.RestClientConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by sr186054 on 10/1/15.
 */
@Component
public class ClouderaClient  {
    private static final Logger LOG = LoggerFactory.getLogger(ClouderaClient.class);

    @Autowired
    @Qualifier("clouderaRestClientConfig")
    private RestClientConfig clientConfig;

    private ClouderaManagerClientBuilder clouderaManagerClientBuilder;


    public ClouderaClient(){


    }
    public ClouderaClient(RestClientConfig clientConfig){

this.clientConfig = clientConfig;
    }
    @PostConstruct
        public void setClouderaManagerClientBuilder(){
        String host = clientConfig.getServerUrl();
        String portString = clientConfig.getPort();
        if(StringUtils.isBlank(portString)){
            portString = "7180";
        }
        Integer port = new Integer(portString);
        String username = clientConfig.getUsername();
        String password = clientConfig.getPassword();
        LOG.info("Created New Cloudera Client for Host ["+host+"], user: ["+username+"]");
        this.clouderaManagerClientBuilder = new ClouderaManagerClientBuilder().withHost(host).withPort(port).withUsernamePassword(username, password);

    }


    public ClouderaRootResource getClouderaResource(){
        ApiRootResource rootResource =   this.clouderaManagerClientBuilder.build();
        return ClouderaRootResourceManager.getRootResource(rootResource);
    }

    public void setClientConfig(RestClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }










}
