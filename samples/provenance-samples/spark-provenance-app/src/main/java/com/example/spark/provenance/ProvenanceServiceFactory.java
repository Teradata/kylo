package com.example.spark.provenance;
/*-
 * #%L
 * kylo-spark-provenance-app
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

import com.thinkbiganalytics.provenance.api.ProvenanceEventService;
import com.thinkbiganalytics.provenance.jms.KyloJmsProvenanceEventService;
import com.thinkbiganalytics.provenance.kafka.KyloKafkaProvenanceEventService;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory to use the correct ProvenanceService implementation based upon the configuration
 */
public class ProvenanceServiceFactory {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceServiceFactory.class);

    public static ProvenanceEventService getProvenanceEventService(SparkProvenanceConfiguration config) {
        ProvenanceEventService service = null;
        Map<String, String> params = new HashMap<>();
        log.info("Creating a new Service of type {} ", config.getType());
        if (config.getType() == SparkProvenanceConfiguration.Type.KAFKA) {
            service = new KyloKafkaProvenanceEventService();
            params.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getConnectionUrl());
            service.configure(params);
        } else if (config.getType() == SparkProvenanceConfiguration.Type.JMS) {
            service = new KyloJmsProvenanceEventService();
            params.put(KyloJmsProvenanceEventService.JMS_URL_CONFIG, config.getConnectionUrl());
            service.configure(params);
        } else {
            throw new UnsupportedOperationException("Unable to create Provenance Event service.  Unknown Provenance Type for: " +config.getType());
        }
        return service;
    }

}
