package com.thinkbiganalytics.provenance.kafka;
/*-
 * #%L
 * kylo-provenance-kafka
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
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;
import com.thinkbiganalytics.nifi.provenance.model.util.GroupedStatsUtil;
import com.thinkbiganalytics.provenance.api.ProvenanceEventService;
import com.thinkbiganalytics.provenance.api.ProvenanceException;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A service allowing you to write Provenance data to Kafka.
 * The items will be sent to 2 Kafka topics serialized as byte[].
 * A subsequent Consumer, or provided System Flow in NiFi will pull the data from these topics and send the events to JMS for Operations Manager to process
 */
public class KyloKafkaProvenanceEventService implements ProvenanceEventService {

    private static final Logger log = LoggerFactory.getLogger(KyloKafkaProvenanceEventService.class);

    private static final String KYLO_BATCH_EVENT_TOPIC = "thinkbig.feed-manager";

    private static final String KYLO_EVENT_STATS_TOPIC = "thinkbig.provenance-event-stats";

    public static final String ACKNOWLEDGE_WAIT_TIME_CONFIG = "ackWaitTime";


    private Producer kafkaProducer = null;

    /**
     * How long to wait if before the produce times out when sending the message
     */
    private long ackWaitTime = 30000;

    private Map<String, String> params = new HashMap<>();

    private Map<String, String> getParameters() {
        return params;
    }


    @Override
    public void configure(Map<String, String> params) {
        if (params.containsKey(ACKNOWLEDGE_WAIT_TIME_CONFIG)) {
            ackWaitTime = Long.valueOf(params.get(ACKNOWLEDGE_WAIT_TIME_CONFIG));
        }
        this.params.putAll(params);
        kafkaProducer = createProducer();
    }

    @Override
    public void sendEvents(List<ProvenanceEventRecordDTO> events) throws ProvenanceException {

        try {
            List<Future<RecordMetadata>> resultFutures = new ArrayList<>();
            ProvenanceEventRecordDTOHolder eventRecordDTOHolder = new ProvenanceEventRecordDTOHolder();
            List<ProvenanceEventRecordDTO> batchEvents = new ArrayList<>();
            for(ProvenanceEventRecordDTO event : events){
                if(!event.isStream()){
                    batchEvents.add(event);
                }
            }
            eventRecordDTOHolder.setEvents(batchEvents);
            byte[] data = SerializationUtils.serialize(eventRecordDTOHolder);
            ProducerRecord<byte[], byte[]> eventsMessage = new ProducerRecord<>(KYLO_BATCH_EVENT_TOPIC, data);
            log.info("Sending {} events to Kafka ", eventRecordDTOHolder);
            resultFutures.add(kafkaProducer.send(eventsMessage));

            AggregatedFeedProcessorStatisticsHolder stats = GroupedStatsUtil.gatherStats(events);
            data = SerializationUtils.serialize(stats);
            ProducerRecord<byte[], byte[]> statsMessage = new ProducerRecord<>(KYLO_EVENT_STATS_TOPIC, data);
            resultFutures.add(kafkaProducer.send(statsMessage));
            processAcks(resultFutures);

        } catch (Exception e) {
            throw new ProvenanceException(e);
        }
    }


    @Override
    public void closeConnection() {
        kafkaProducer.close();
    }

    private Producer<byte[], byte[]> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                  getParameters().get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "kafka.client");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        log.info("Creating KAFKA Producer {} ", props);

        return new KafkaProducer<>(props);
    }

    private int processAcks(List<Future<RecordMetadata>> sendFutures) {
        boolean exceptionThrown = false;
        int idx = 0;
        try {
            for (int i = 0; i < sendFutures.size() && !exceptionThrown; i++) {
                Future<RecordMetadata> future = sendFutures.get(i);
                future.get(this.ackWaitTime, TimeUnit.MILLISECONDS);
                idx++;
            }
        } catch (InterruptedException e) {
            exceptionThrown = true;
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for acks from Kafka");
        } catch (ExecutionException e) {
            exceptionThrown = true;
            log.error("Failed while waiting for acks from Kafka", e);
        } catch (TimeoutException e) {
            exceptionThrown = true;
            log.error("Timed out while waiting for acks from Kafka", e);
        }
        return idx;
    }

}
