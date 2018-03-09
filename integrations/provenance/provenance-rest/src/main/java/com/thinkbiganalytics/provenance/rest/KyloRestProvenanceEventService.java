package com.thinkbiganalytics.provenance.rest;
/*-
 * #%L
 * kylo-provenance-rest
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

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.provenance.api.ProvenanceEventService;
import com.thinkbiganalytics.provenance.api.ProvenanceException;
import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class KyloRestProvenanceEventService implements ProvenanceEventService {

    public static String HOST_CONFIG = "host";
    public static String PORT_CONFIG = "port";
    public static String USERNAME_CONFIG = "username";
    public static String PASSWORD_CONFIG = "password";
    public static String KEYSTORE_ON_CLASSPATH_CONFIG = "keystoreOnClassPath";
    public static String KEYSTORE_PATH_CONFIG = "keystorePath";
    public static String KEYSTORE_PASSWORD_CONFIG = "keystorePassword";
    public static String KEYSTORE_TYPE_CONFIG = "keystoreType";

    public static String TRUSTSTORE_PATH_CONFIG = "truststorePath";
    public static String TRUSTSTORE_PASSWORD_CONFIG = "truststorePassword";
    public static String TRUSTSTORE_TYPE_CONFIG = "trustStoreType";

    private JerseyRestClient restClient;

    private static String PROVENANCE_REST_PATH = "/proxy/v1/provenance";



    @Override
    public void configure(Map<String, String> params) {

        String host = params.get(HOST_CONFIG);
        char[] pass = null;
        if (params.containsKey(PASSWORD_CONFIG)) {
            pass = params.get(PASSWORD_CONFIG).toCharArray();
        }
        String port = params.get(PORT_CONFIG);
        String username = params.get(USERNAME_CONFIG);
        JerseyClientConfig jerseyClientConfig= new JerseyClientConfig(host,username,pass);
        if(StringUtils.isNotBlank(port)) {
            jerseyClientConfig.setPort(Integer.valueOf(port));
        }
        if(params.containsKey(TRUSTSTORE_PATH_CONFIG)){
            jerseyClientConfig.setTruststorePath(params.get(TRUSTSTORE_PATH_CONFIG));
        }
        if(params.containsKey(TRUSTSTORE_PASSWORD_CONFIG)){
            jerseyClientConfig.setTruststorePassword(params.get(TRUSTSTORE_PASSWORD_CONFIG).toCharArray());
        }
        if(params.containsKey(TRUSTSTORE_TYPE_CONFIG)){
            jerseyClientConfig.setTrustStoreType(params.get(TRUSTSTORE_TYPE_CONFIG));
        }

        if(params.containsKey(KEYSTORE_ON_CLASSPATH_CONFIG)){
            jerseyClientConfig.setKeystoreOnClasspath(BooleanUtils.toBoolean(params.get(KEYSTORE_ON_CLASSPATH_CONFIG)));
        }

        if(params.containsKey(KEYSTORE_PATH_CONFIG)){
            jerseyClientConfig.setKeystorePath(params.get(KEYSTORE_PATH_CONFIG));
        }
        if(params.containsKey(KEYSTORE_PASSWORD_CONFIG)){
            jerseyClientConfig.setKeystorePassword(params.get(KEYSTORE_PASSWORD_CONFIG).toCharArray());
        }
        if(params.containsKey(KEYSTORE_TYPE_CONFIG)){
            jerseyClientConfig.setKeystoreType(params.get(KEYSTORE_TYPE_CONFIG));
        }
        restClient = new JerseyRestClient(jerseyClientConfig);
    }



    @Override
    public void sendEvents(List<ProvenanceEventRecordDTO> events) throws ProvenanceException {
        ProvenanceEventRecordDTOHolder eventRecordDTOHolder = new ProvenanceEventRecordDTOHolder();
        eventRecordDTOHolder.setEvents(events);
        restClient.post(PROVENANCE_REST_PATH,eventRecordDTOHolder);
    }

    @Override
    public void closeConnection() {

    }
}
