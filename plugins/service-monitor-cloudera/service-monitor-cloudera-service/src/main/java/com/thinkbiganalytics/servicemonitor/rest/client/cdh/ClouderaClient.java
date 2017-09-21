package com.thinkbiganalytics.servicemonitor.rest.client.cdh;

/*-
 * #%L
 * thinkbig-service-monitor-cloudera
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.cloudera.api.ApiRootResource;
import com.cloudera.api.ClouderaManagerClientBuilder;
import com.thinkbiganalytics.servicemonitor.rest.client.RestClientConfig;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Cloudera REST API client.
 */
@Component
public class ClouderaClient {

    private static final Logger LOG = LoggerFactory.getLogger(ClouderaClient.class);

    @Inject
    @Qualifier("clouderaRestClientConfig")
    private RestClientConfig clientConfig;

    private ClouderaManagerClientBuilder clouderaManagerClientBuilder;

    private ClouderaRootResource clouderaRootResource;

    private AtomicBoolean creatingResource = new AtomicBoolean(false);

    private Integer clientAttempts = 0;


    public ClouderaClient() {

    }

    public ClouderaClient(RestClientConfig clientConfig) {

        this.clientConfig = clientConfig;
    }

    @PostConstruct
    public void setClouderaManagerClientBuilder() {
        String host = clientConfig.getServerUrl();
        String portString = clientConfig.getPort();
        if (StringUtils.isBlank(portString)) {
            portString = "7180";
        }
        Integer port = new Integer(portString);
        String username = clientConfig.getUsername();
        String password = clientConfig.getPassword();

        LOG.info("Created New Cloudera Client for Host [" + host + ":" + port + "], user: [" + username + "]");
        clouderaManagerClientBuilder = new ClouderaManagerClientBuilder().withHost(host).withPort(port).withUsernamePassword(username, password);
        if (BooleanUtils.isTrue(clientConfig.getEnableTLS())) {
            clouderaManagerClientBuilder.enableTLS();
        }
    }


    public ClouderaRootResource getClouderaResource() {
        if (clouderaRootResource == null) {
            this.clientAttempts++;
        }
        if (clouderaRootResource == null && !creatingResource.get()) {
            creatingResource.set(true);
            try {
                ApiRootResource rootResource = this.clouderaManagerClientBuilder.build();
                clouderaRootResource = ClouderaRootResourceManager.getRootResource(rootResource);
                LOG.info("Successfully Created Cloudera Client");
            } catch (Throwable e) {
                LOG.error("Failed to create Cloudera Client", e);
                creatingResource.set(false);
            }
        }
        if (clientAttempts >= 3) {
            //rest
            clientAttempts = 0;
            creatingResource.set(false);
        }
        return clouderaRootResource;
    }

    public ClouderaManagerClientBuilder getClouderaManagerClientBuilder() {
        return clouderaManagerClientBuilder;
    }
}
