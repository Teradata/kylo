package com.thinkbiganalytics.nifi.provenance;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.v2.writer.ProvenanceEventActiveMqWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Consume the Events added by the ProvenancneEventAggregator and send to JMS
 *
 * Created by sr186054 on 8/14/16.
 */
public class JmsBatchProvenanceEventFeedConsumer extends BatchedQueue<ProvenanceEventRecordDTO> {

    private static final Logger log = LoggerFactory.getLogger(JmsBatchProvenanceEventFeedConsumer.class);

    private StreamConfiguration configuration;

    private ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter;

    /**
     * The maximum number of events to be contained in the ProvenanceEventRecordDTOHolder for sending off to JMS
     */
    private Integer MAXIMUM_JMS_GROUP_SIZE = 50;

    public JmsBatchProvenanceEventFeedConsumer(StreamConfiguration configuration, ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter, BlockingQueue<ProvenanceEventRecordDTO> queue
    ) {
        super(configuration.getJmsBatchDelay(), queue);
        this.configuration = configuration;
        this.provenanceEventActiveMqWriter = provenanceEventActiveMqWriter;
    }

    @Override
    public void processQueue(List<ProvenanceEventRecordDTO> elements) {
        if (elements != null) {
            log.info("processQueue for {} Nifi Events ", elements.size());
            //batch these up int groups of 50 so jms can handle the threading
            Lists.partition(elements, MAXIMUM_JMS_GROUP_SIZE).forEach(eventsSubList -> {
                ProvenanceEventRecordDTOHolder eventRecordDTOHolder = new ProvenanceEventRecordDTOHolder();
                eventRecordDTOHolder.setEvents(Lists.newArrayList(eventsSubList));
                provenanceEventActiveMqWriter.writeBatchEvents(eventRecordDTOHolder);
            });
        }
    }


}

