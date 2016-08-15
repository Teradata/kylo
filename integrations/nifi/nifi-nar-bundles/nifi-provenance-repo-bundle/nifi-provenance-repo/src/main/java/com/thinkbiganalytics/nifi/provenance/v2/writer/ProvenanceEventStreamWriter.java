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
 *
 * Batch Events up and later on determine how to process the events as either Batch or Stream
 *
 * 1. Add:  Add the events to the Queue to be processed
 *    Events are added as "Delayed events" and will be taken from the queue when they are expired (xx time after they have been added) configured by the StreamConfiguration
 * 2. Consume: The Consumer will then group the events together using the ProvenanceEventCollector
 *    The default collector is to group the events by Feed and then Processor Id
 * 3. Process: The Consumer will then send the Group off to be processed by the ProvenanceEventProcessor
 *    The ProvenanceEventProcessor will determine if the the events are either a Stream or a Batch based upon the StreamConfiguration
 *
 *
 *
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
        log.info("Writing Event {} ", dto.getEventId());
        return dto.getEventId();
    }


}
