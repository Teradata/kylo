package com.thinkbiganalytics.nifi.provenance.v2.writer;

import com.thinkbiganalytics.nifi.provenance.ProvenanceEventAggregator;
import com.thinkbiganalytics.nifi.provenance.StreamConfiguration;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.v2.ProvenanceEventRecordConverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

/**
 * Created by sr186054 on 8/11/16.
 *
 * Batch Events up and later on determine how to process the events as either Batch or Stream
 *
 * 1. Add:  Add the events to the Queue to be processed Events are added as "Delayed events" and will be taken from the queue when they are expired (xx time after they have been added) configured by
 * the StreamConfiguration
 * 2. Consume: The Consumer will then group the events together using the ProvenanceEventCollector The default collector is to group the events by Feed and then Processor Id
 * 3. Process: The Consumer will then send the Group off to be processed by the ProvenanceEventProcessor The ProvenanceEventProcessor will determine if the the events are either a Stream or a Batch based
 * upon the StreamConfiguration. StreamConfiguration checks 3 parameters to determine if things are a stream or batch
 *   1) processDelay  - how long to wait before grouping events together to determine if they are stream or batch
 *   2) maxTimeBetweenEventsMillis - Max time between events for a given Feed Processor considered a Batch Anything under this time will be considered a Stream provided it passes the numberOfEventsToConsiderAStream
 *   3) numberOfEventsToConsiderAStream  - Number of events needed to be in queue/processing to be considered a stream that fall within the maxTimeBetweenEventsMillis for processing on a given Processor for a specific Feed
 */
@Component
public class ThinkbigProvenanceEventWriter {

    private static final Logger log = LoggerFactory.getLogger(ThinkbigProvenanceEventWriter.class);

    @Autowired
    private ProvenanceEventAggregator producer;

    @Autowired
    private StreamConfiguration configuration;

    /**
     * Comma separated string of JMS queu names that should skip Kylo Processing
     */
    @Value("${thinkbig.provenance.jmsQueueSkip:}")
    private String jmsQueueNamesToSkip;

    /**
     * Resolved list of queue names parsed from the {@code jmsQueueNamesToSkip}
     */
    private List<String> queueNamesToSkip;


    public ThinkbigProvenanceEventWriter() {

    }


    public ThinkbigProvenanceEventWriter(StreamConfiguration configuration) {
        this.configuration = configuration;

    }

    @PostConstruct
    public void postConstruct() {
        log.info("New ProvenanceEventWriter with Stream Configuration {}, and aggregator: {} ", configuration, producer);


        log.info("Queues names to skip {}", jmsQueueNamesToSkip);
        if(StringUtils.isNotBlank(jmsQueueNamesToSkip)) {
            queueNamesToSkip = Arrays.asList(jmsQueueNamesToSkip.split(","));
            queueNamesToSkip.replaceAll(String::trim);
        }
        else {
            queueNamesToSkip = Collections.emptyList();
        }
        log.info("Going to skip following queues {}", queueNamesToSkip);

    }

    /**
     *  Note: Every Exception here needs to be caught.  Throwing a runtime exception will cause NiFi to abort processing and loose information
     */
    public Long writeEvent(ProvenanceEventRecord event, long eventId) {
        try {

            ProvenanceEventRecordDTO dto = ProvenanceEventRecordConverter.convert(event);
            if(isSkipProcessing(dto)){
                log.trace("Skipping event {}", event);
                return eventId;
            }
            else {
                log.trace("Processing event {}", event);
                dto.setEventId(eventId);
                producer.prepareAndAdd(dto);
                return dto.getEventId();
            }
        }catch (Exception e){
            log.error("ERROR occurred processing event ",event);
        }
        return null;
    }

    /**
     * Check to see if the Attributes contain a "Destination Name" property whose value matches a defined Queue name found in the config.properties file that is to be skipped from Kylo Processing
     * @param dto
     * @return
     */
    private boolean isSkipProcessing(ProvenanceEventRecordDTO dto) {
        log.trace("Event attribute map {}", dto.getAttributeMap());
        String queueToSkip = dto.getAttributeMap().getOrDefault("jms_destination", null);
        return (queueToSkip != null && queueNamesToSkip.contains(queueToSkip));

    }


}