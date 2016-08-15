import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.nifi.flow.controller.NifiFlowClient;
import com.thinkbiganalytics.nifi.provenance.StreamConfiguration;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flow.NifiFlowCache;
import com.thinkbiganalytics.nifi.provenance.v2.writer.ProvenanceEventStreamWriter;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.apache.nifi.provenance.StandardProvenanceEventRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Mockito.when;

/**
 * Created by sr186054 on 8/12/16.
 */
public class TestProvenanceFlow {

    private static final Logger log = LoggerFactory.getLogger(TestProvenanceFlow.class);


    /**
     * The static flow/Graph of processors and connections
     */
    private NifiFlowProcessGroup flow;


    private AtomicLong processorId = new AtomicLong(0L);


    private void connect(NifiFlowProcessGroup group, NifiFlowProcessor source, NifiFlowProcessor dest) {
        source.getDestinations().add(dest);
        dest.getSources().add(source);
        source.getDestinationIds().add(dest.getId());
        dest.getSourceIds().add(source.getId());
        source.setProcessGroup(group);
        dest.setProcessGroup(group);
        group.getProcessorMap().put(source.getId(), source);
        group.getProcessorMap().put(dest.getId(), dest);
    }

    private NifiFlowProcessor processor() {
        return new NifiFlowProcessor(UUID.randomUUID().toString(), "Processor " + processorId.incrementAndGet());
    }


    /**
     * Build a simple flow/tree
     *
     * P1 P2 P3     P4 P5 P6
     */
    public NifiFlowProcessGroup buildFlow() {

        NifiFlowProcessGroup group = new NifiFlowProcessGroup(UUID.randomUUID().toString(), "Group");
        group.setFeedName("TestFeed");

        NifiFlowProcessor processor1 = processor();
        NifiFlowProcessor processor2 = processor();
        NifiFlowProcessor processor3 = processor();
        NifiFlowProcessor processor4 = processor();
        NifiFlowProcessor processor5 = processor();
        NifiFlowProcessor processor6 = processor();
        group.getStartingProcessors().add(processor1);
        connect(group, processor1, processor2);
        connect(group, processor2, processor3);
        connect(group, processor2, processor4);
        connect(group, processor4, processor5);
        connect(group, processor5, processor6);
        return group;
    }


    /**
     * get the static Flow
     */
    public NifiFlowProcessGroup getFlow() {
        if (flow == null) {
            flow = buildFlow();
        }
        return flow;
    }


    /**
     * Track all events completed by flow just to doublecheck and ensure processing is correct
     */
    private Map<String, List<ProvenanceEventRecord>> completedEvents = new HashMap<>();

    private class RunningFlow {

        NifiFlowProcessGroup flow;
        Set<String> currentProcessIds;

        String id;

        public RunningFlow(NifiFlowProcessGroup flow) {
            this.flow = flow;
            this.currentProcessIds = new HashSet<>();
        }


        public String getRandomStart() {
            return Lists.newArrayList(flow.getStartingProcessors()).get(0).getId();
        }

        public void run() {
            this.id = UUID.randomUUID().toString();
            start(getRandomStart());
        }

        public void start(String processorId) {
            log.info("Starting processor {} ({}) for flow {} ", flow.getProcessor(processorId).getName(), processorId, flow.getId());
            currentProcessIds.add(processorId);
        }

        public List<ProvenanceEventRecord> next() {
            List<ProvenanceEventRecord> completedEvents = new ArrayList<>();
            Set<String> currentIds = Sets.newHashSet(currentProcessIds);
            for (String p : currentIds) {
                if (flow.getProcessor(p).isStart()) {
                    completedEvents.add(complete(p, ProvenanceEventType.RECEIVE));
                } else if (flow.getProcessor(p).isEnd()) {
                    completedEvents.add(complete(p, ProvenanceEventType.DROP));
                } else {
                    completedEvents.add(complete(p, ProvenanceEventType.ATTRIBUTES_MODIFIED));

                }
            }
            return completedEvents;
        }

        public ProvenanceEventRecord complete(String processorId, ProvenanceEventType eventType) {
            log.info("Complete processor {} ({}) for flow {} ", flow.getProcessor(processorId).getName(), processorId, flow.getId());
            currentProcessIds.remove(processorId);
            Set<String> destinations = flow.getProcessor(processorId).getDestinationIds();
            //
            if (destinations != null && !destinations.isEmpty()) {
                destinations.forEach(destinationId -> {
                    //run all trees
                    start(destinationId);
                });
            }

            ProvenanceEventRecord event = new StandardProvenanceEventRecord.Builder().setEventType(eventType)
                .setComponentId(processorId)
                .setFlowFileUUID(id)
                .setComponentType("PROCESSOR")
                .setTransitUri("http://test.com")
                .setCurrentContentClaim("container", "section", "identifier", 0L, 1000L)
                .setEventTime(System.currentTimeMillis())
                .setDetails(flow.getProcessor(processorId).getName())
                .build();
            if (!completedEvents.containsKey(processorId)) {
                completedEvents.put(processorId, new ArrayList<>());
            }
            completedEvents.get(processorId).add(event);
            return event;
        }

        public Set<String> getCurrentProcessIds() {
            return currentProcessIds;
        }


    }

    private List<RunningFlow> activeFlows = new ArrayList<>();

    private RunningFlow newFlowFile() {
        RunningFlow flow = new RunningFlow(getFlow());
        flow.run();
        return flow;
    }


    private class TestProducer implements Runnable {

        ProvenanceEventStreamWriter writer;
        int counter = 0;

        int modulo = 3; // how often to create flow files

        /**
         * The Maximum number of flow files to produce
         */
        int max = 100;

        /**
         * How long to wait before moving on to the next processor in the flow or starting the next new flow
         */
        long waitTime = 20L;

        public TestProducer(Long waitTime, StreamConfiguration configuration) {
            this.waitTime = waitTime;
            writer = new ProvenanceEventStreamWriter(configuration);
        }


        @Override
        public void run() {
            while (true) {

                for (RunningFlow flow : activeFlows) {
                    //get all completed events and then simulate Nifi Provenance writing.
                    List<ProvenanceEventRecord> completedEvents = flow.next();
                    if (completedEvents != null) {
                        completedEvents.forEach(event -> writer.writeEvent(event));
                    }
                }
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (counter % modulo == 0 && counter < max) {
                    log.info("NEW FLOW FILE " + (activeFlows.size() + 1));
                    //start a new flow every 5 iterations
                    activeFlows.add(newFlowFile());
                }
                counter++;
            }
        }
    }

    /**
     * Mock up the Job Rep
     */
    @Before
    public void setupMockNifiFlowClient() {
        MockitoAnnotations.initMocks(this);
        NifiFlowClient mockClient = Mockito.mock(NifiFlowClient.class);
        when(mockClient.getAllFlows()).thenReturn(Lists.newArrayList(getFlow()));
        NifiFlowCache.instance().setNifiFlowClient(mockClient);
        NifiFlowCache.instance().loadAll();





    }



    @Test
    public void testStream() {
        //A Stream is when there are 5 or more events spaced less than 1 sec apart coming in for a Processor for a Flow tied to a Feed
        TestProducer producer = new TestProducer(20L, new StreamConfiguration.Builder().maxTimeBetweenEvents(1000L).numberOfEventsForStream(5).build());
        producer.run();
    }

    @Test
    public void testBatch() {
        //A Stream is when there are 5 or more events spaced less than 1 sec apart coming in for a Processor for a Flow tied to a Feed
        TestProducer producer = new TestProducer(500L, new StreamConfiguration.Builder().maxTimeBetweenEvents(1000L).numberOfEventsForStream(5).build());
        producer.run();
    }



}
