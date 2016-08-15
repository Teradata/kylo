package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.collector.ProvenanceEventCollector;
import com.thinkbiganalytics.nifi.provenance.collector.ProvenanceEventFeedCollector;
import com.thinkbiganalytics.nifi.provenance.model.DelayedProvenanceEvent;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.processor.ProvenanceEventProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/14/16.
 */
public class DelayedProvenanceEventFeedConsumer implements Runnable {

    private StreamConfiguration configuration;

    private BlockingQueue<DelayedProvenanceEvent> queue;

    private ProvenanceEventCollector eventCollector;

    public DelayedProvenanceEventFeedConsumer(StreamConfiguration configuration, BlockingQueue<DelayedProvenanceEvent> queue) {
        this.queue = queue;
        this.configuration = configuration;
        //Group events by Feed before processing
        eventCollector = new ProvenanceEventFeedCollector();
    }


    private List<ProvenanceEventRecordDTO> takeAll() {
        List<DelayedProvenanceEvent> expired = new ArrayList<>();
        queue.drainTo(expired);
        return expired.stream().map(delayed -> delayed.getEvent()).collect(Collectors.toList());
    }


    private void process() {

        //everything in queue collect and process
        List<ProvenanceEventRecordDTO> events = takeAll();
        //collect the events into the correct grouping (by Feed)
        if (events != null && !events.isEmpty()) {
            //process each group of events
            //Grouped by root processorId with list of events from various FlowFiles
            eventCollector.collect(events).entrySet().stream().forEach(entry -> {
                //This could be broken out into multiple threads if desired
                //Send the List of events, grouped by Feed (root processorId) on to be processed to determine if they are stream or batch
                new ProvenanceEventProcessor(configuration).process(entry.getValue());
            });
        }


    }


    public void run() {
        while (true) {
            try {
                //Wait a period of time before processing to group all expired events together
                Thread.sleep(configuration.getProcessDelay());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            process();
        }
    }


}
