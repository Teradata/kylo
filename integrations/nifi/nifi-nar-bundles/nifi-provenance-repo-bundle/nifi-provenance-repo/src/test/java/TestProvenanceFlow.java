import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.nifi.flow.controller.NifiFlowClient;
import com.thinkbiganalytics.nifi.provenance.StreamConfiguration;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flow.NifiFlowCache;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.FlowFileGuavaCache;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
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
    private NifiFlowProcessGroup feed1;

    private NifiFlowProcessGroup feed2;


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
     *     P1
     *     P2
     *   P3     P4
     * P9  P10          P5
     *              P6
     */
    public NifiFlowProcessGroup buildFlow1() {

        NifiFlowProcessGroup group = new NifiFlowProcessGroup(UUID.randomUUID().toString(), "Group");

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
        group.setFeedName("TestFeed1 - " + group.getProcessorMap().size() + " processors (" + processor1.getName() + " - " + processor6.getName() + ") ");
        return group;
    }


    /**
     * P1 P2    P3 P4   P8    P9 P5 P6  P7
     */
    public NifiFlowProcessGroup buildFlow2() {

        NifiFlowProcessGroup group = new NifiFlowProcessGroup(UUID.randomUUID().toString(), "Group");
        group.setFeedName("TestFeed2");

        NifiFlowProcessor processor1 = processor();
        NifiFlowProcessor processor2 = processor();
        NifiFlowProcessor processor3 = processor();
        NifiFlowProcessor processor4 = processor();
        NifiFlowProcessor processor5 = processor();
        NifiFlowProcessor processor6 = processor();
        NifiFlowProcessor processor7 = processor();
        NifiFlowProcessor processor8 = processor();
        NifiFlowProcessor processor9 = processor();
        group.getStartingProcessors().add(processor1);
        connect(group, processor1, processor2);
        connect(group, processor1, processor3);
        connect(group, processor2, processor4);
        connect(group, processor4, processor5);
        connect(group, processor5, processor6);
        connect(group, processor5, processor7);
        connect(group, processor3, processor8);
        connect(group, processor3, processor9);
        group.setFeedName("TestFeed2 - " + group.getProcessorMap().size() + " processors (" + processor1.getName() + " - " + processor9.getName() + ") ");
        return group;
    }


    /**
     * get the static Flow
     */
    public NifiFlowProcessGroup getFlow() {
        if (feed1 == null) {
            feed1 = buildFlow1();
            feed2 = buildFlow2();
        }
        int feedNumber = ThreadLocalRandom.current().nextInt(1, 3);
        if (feedNumber == 1) {
            return feed1;
        } else {
            return feed2;
        }
    }


    /**
     * Track all events completed by flow just to doublecheck and ensure processing is correct
     */
    private Map<String, List<ProvenanceEventRecord>> completedEvents = new HashMap<>();

    private class RunningFlow {

        NifiFlowProcessGroup flow;
        Set<String> currentProcessIds;

        String id;
        private Long activeEvents = 0L;

        private boolean registeredAsFinished;



        public boolean isRegisteredAsFinished() {
            return registeredAsFinished;
        }

        public void setRegisteredAsFinished(boolean registeredAsFinished) {
            this.registeredAsFinished = registeredAsFinished;
        }

        public RunningFlow(NifiFlowProcessGroup flow) {
            this.flow = flow;
            this.currentProcessIds = new HashSet<>();
        }

        public String getId(){
            return this.id;
        }


        public String getRandomStart() {
            return Lists.newArrayList(flow.getStartingProcessors()).get(0).getId();
        }

        public void run() {
            this.id = UUID.randomUUID().toString();
            start(getRandomStart());
        }

        public void start(String processorId) {
            //log.info("Starting processor {} ({}) for flow {} ", flow.getProcessor(processorId).getName(), processorId, flow.getId());
            currentProcessIds.add(processorId);
            activeEvents++;
        }

        public List<ProvenanceEventRecord> next() {
            List<ProvenanceEventRecord> completedEvents = new ArrayList<>();
            Set<String> currentIds = Sets.newHashSet(currentProcessIds);
            for (String p : currentIds) {
                Set<String> destinations = flow.getProcessor(p).getDestinationIds();


                if (flow.getProcessor(p).isStart()) {
                    completedEvents.add(complete(p, ProvenanceEventType.RECEIVE));
                } else if (flow.getProcessor(p).isEnd() || (activeEvents ==1 && destinations.size() == 0)) {
                    completedEvents.add(complete(p, ProvenanceEventType.DROP));
                } else {
                    completedEvents.add(complete(p, ProvenanceEventType.ATTRIBUTES_MODIFIED));
                }
            }
            return completedEvents;
        }

        public ProvenanceEventRecord complete(String processorId, ProvenanceEventType eventType) {
          // log.info("Complete processor {} ({}) for flow {} ", flow.getProcessor(processorId).getName(), processorId, flow.getId());
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
            activeEvents--;
            eventsFinished.incrementAndGet();
            return event;
        }

        public Set<String> getCurrentProcessIds() {
            return currentProcessIds;
        }

        public Long getActiveEvents(){
            return activeEvents;
        }


    }


    private AtomicInteger totalFinishedFlows = new AtomicInteger(0);
    private AtomicInteger totalStartedFlows = new AtomicInteger(0);
    private AtomicInteger finishedRuns = new AtomicInteger(0);
    private AtomicLong eventsFinished = new AtomicLong(0);



    private RunningFlow newFlowFile() {
        RunningFlow flow = new RunningFlow(getFlow());
        flow.run();
        return flow;
    }


    private class TestProducer implements Runnable {

        ProvenanceEventStreamWriter writer;
        private int finishedflows = 0;
        private List<RunningFlow> activeFlows = new ArrayList<>();
        int counter = 0;

        int modulo = 3; // how often to create flow files

        /**
         * The Maximum number of flow files to produce
         */
        int max = 100;

        /**
         * How long to wait before moving on to the next processor in the flow or starting the next new flow
         */
        long waitTime = 1L;

        public TestProducer(Long waitTime, int max,StreamConfiguration configuration) {
            this.waitTime = waitTime;
            this.max = max;
            writer = new ProvenanceEventStreamWriter(configuration);
        }


        @Override
        public void run() {
            long start = System.currentTimeMillis();
            while (finishedflows <=max) {

                List<RunningFlow> finishedFlows  = new ArrayList<>();
                for (RunningFlow flow : activeFlows) {
                    //get all completed events and then simulate Nifi Provenance writing.
                    List<ProvenanceEventRecord> completedEvents = flow.next();
                    if (completedEvents != null && !completedEvents.isEmpty()) {
                        completedEvents.forEach(event -> writer.writeEvent(event));
                    }
                    else {
                        if(flow.getActiveEvents()== 0L) {
                            if(!flow.isRegisteredAsFinished()) {
                                flow.setRegisteredAsFinished(true);
                                finishedflows++;
                                finishedFlows.add(flow);
                                log.info("Finished flow {}    {}/{}", flow.getId(), totalStartedFlows.get(), totalFinishedFlows.incrementAndGet());
                            }
                        }
                    }
                }
                //clear finished
                for(RunningFlow finishedFlow : finishedFlows){
                    activeFlows.remove(finishedFlow);
                }

                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (counter % modulo == 0 && finishedflows <=max) {
                    log.info("NEW FLOW FILE " + finishedflows + 1);
                    //start a new flow every 5 iterations
                    totalStartedFlows.incrementAndGet();
                    activeFlows.add(newFlowFile());
                }
                counter++;
            }
            long end = System.currentTimeMillis();
            log.info("TOTAL TIME to process {}  = flows: {} ms.   Total counts: {}/{} ",finishedflows,(end-start),totalFinishedFlows.get(),totalStartedFlows.get());
            FlowFileGuavaCache.instance().printSummary();
            finishedRuns.incrementAndGet();
        }
    }

    /**
     * Mock up the Job Rep
     */
    @Before
    public void setupMockNifiFlowClient() {
        MockitoAnnotations.initMocks(this);
        NifiFlowClient mockClient = Mockito.mock(NifiFlowClient.class);
        getFlow();
        when(mockClient.getAllFlows()).thenReturn(Lists.newArrayList(feed1, feed2));
        NifiFlowCache.instance().setNifiFlowClient(mockClient);
        NifiFlowCache.instance().loadAll();





    }

    public class Runner implements Runnable {
        int runs = 0;
        public Runner(int runs){
            this.runs = runs;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            while(finishedRuns.get() <runs){

                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            long end = System.currentTimeMillis();
            log.info("TOTAL TIME to process {} ms  flows Total counts: Flow files: {}/{}, Events: {} ",(end-start),totalFinishedFlows.get(),totalStartedFlows.get(),eventsFinished.get());
        }
    }



    @Test
    public void testStream() {
        //A Stream is when there are 5 or more events spaced less than 1 sec apart coming in for a Processor for a Flow tied to a Feed
        TestProducer producer = new TestProducer(1L, 1000000, new StreamConfiguration.Builder().maxTimeBetweenEvents(3000L).numberOfEventsForStream(5).build());
        Thread t1 = new Thread(producer);
        TestProducer producer2 = new TestProducer(1000L, 1000,new StreamConfiguration.Builder().maxTimeBetweenEvents(1000L).numberOfEventsForStream(5).build());
        Thread t2 = new Thread(producer2);
        TestProducer producer3 = new TestProducer(2000L, 100000, new StreamConfiguration.Builder().maxTimeBetweenEvents(3000L).numberOfEventsForStream(5).build());
        Thread t3 = new Thread(producer3);
        t1.start();
        t2.start();
        t3.start();
        Runner runner = new Runner(3);
        runner.run();

    }

    @Test
    public void testBatch() {
        //A Stream is when there are 5 or more events spaced less than 1 sec apart coming in for a Processor for a Flow tied to a Feed
        TestProducer producer = new TestProducer(500L, 10,new StreamConfiguration.Builder().maxTimeBetweenEvents(1000L).numberOfEventsForStream(5).build());
        producer.run();
    }





}
