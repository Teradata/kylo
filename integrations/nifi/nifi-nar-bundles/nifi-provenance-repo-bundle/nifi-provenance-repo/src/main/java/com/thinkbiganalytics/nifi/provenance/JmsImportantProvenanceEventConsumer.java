package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.v2.writer.ProvenanceEventActiveMqWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Consume the Events added by the DelayedProvenanceEventProducer after the delay has expired After consuming the events it will process each event by: - Collect the events in a group by Feed. - for
 * each group Process the events and determine if the event is a Stream (Rapid fire of events) or a Batch based upon the StreamConfiguration that was provided.
 *
 * Created by sr186054 on 8/14/16.
 */
public class JmsImportantProvenanceEventConsumer extends BatchedQueue<ProvenanceEventRecordDTO> {

    private static final Logger log = LoggerFactory.getLogger(JmsImportantProvenanceEventConsumer.class);

    private StreamConfiguration configuration;


    private ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter;

    public JmsImportantProvenanceEventConsumer(StreamConfiguration configuration, ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter, BlockingQueue<ProvenanceEventRecordDTO> queue
    ) {
        super(configuration.getJmsBatchDelay(), queue);
        this.configuration = configuration;
        this.provenanceEventActiveMqWriter = provenanceEventActiveMqWriter;
    }

    @Override
    public void processQueue(List<ProvenanceEventRecordDTO> elements) {
        if (elements != null) {
            ProvenanceEventRecordDTOHolder holder = new ProvenanceEventRecordDTOHolder();
            holder.setEvents(elements);
            provenanceEventActiveMqWriter.writeEvents(holder);
        }
    }


}
