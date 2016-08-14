package com.thinkbiganalytics.nifi.provenance.v2.writer;

import com.thinkbiganalytics.nifi.provenance.DelayedProvenanceEventFeedConsumer;
import com.thinkbiganalytics.nifi.provenance.DelayedProvenanceEventProducer;
import com.thinkbiganalytics.nifi.provenance.StreamConfiguration;
import com.thinkbiganalytics.nifi.provenance.model.DelayedProvenanceEvent;
import com.thinkbiganalytics.nifi.provenance.v2.ProvenanceEventConverter;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

/**
 * Created by sr186054 on 8/11/16.
 */
public class ProvenanceEventStreamWriter extends AbstractProvenanceEventWriter {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventStreamWriter.class);

    ProvenanceEventRecordFileWriter fileWriter;

    private BlockingQueue<DelayedProvenanceEvent> queue;
    private DelayedProvenanceEventProducer producer;
    private StreamConfiguration configuration;

    public ProvenanceEventStreamWriter(StreamConfiguration configuration) {
        queue = new DelayQueue<>();
        producer = new DelayedProvenanceEventProducer(configuration, queue);
        //Consume the Events on a different thread than the NiFi event processing

        Thread consumerThread = new Thread(new DelayedProvenanceEventFeedConsumer(configuration, queue));
        consumerThread.start();
    }


    public Long writeEvent(ProvenanceEventRecord event) {
        ProvenanceEventDTO dto = ProvenanceEventConverter.convert(event);
        dto.setEventId(eventIdIncrementer.incrementAndGet());
        producer.add(dto);
        return dto.getEventId();
    }


}
