package com.thinkbiganalytics.jobrepo.nifi;

import com.thinkbiganalytics.jobrepo.JobRepoApplicationStartupListener;
import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileComponent;
import com.thinkbiganalytics.jobrepo.nifi.model.NifiJobExecution;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.jobrepo.nifi.provenance.FlowFileEventProvider;
import com.thinkbiganalytics.jobrepo.nifi.provenance.InMemoryFlowFileEventProvider;
import com.thinkbiganalytics.jobrepo.nifi.provenance.NifiComponentFlowData;
import com.thinkbiganalytics.jobrepo.nifi.provenance.ProvenanceEventApplicationStartupListener;
import com.thinkbiganalytics.jobrepo.nifi.provenance.ProvenanceEventListener;
import com.thinkbiganalytics.jobrepo.nifi.provenance.ProvenanceEventReceiver;
import com.thinkbiganalytics.jobrepo.nifi.provenance.ProvenanceFeedManager;
import com.thinkbiganalytics.jobrepo.repository.JobRepository;
import com.thinkbiganalytics.jobrepo.repository.dao.NifiJobRepository;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;

import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.apache.nifi.web.api.entity.AboutEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Created by sr186054 on 5/25/16.
 */
public class ProvenanceEventReceiverTest {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventReceiverTest.class);

    private ProvenanceEventReceiver provenanceEventReceiver = new ProvenanceEventReceiver();


    private ProvenanceEventListener provenanceEventListener;

    private ProvenanceFeedManager provenanceFeedManager;

    private FlowFileEventProvider flowFileEventProvider;

    private NifiComponentFlowData nifiComponentFlowData;

    @Mock
    private ProvenanceEventApplicationStartupListener provenanceEventStartupListener;

    @Mock
    private JobRepoApplicationStartupListener jobRepoApplicationStartupListener;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private NifiJobRepository nifiJobRepository;

    @Mock
    private NifiRestClient nifiRestClient;


    private AtomicLong eventId = new AtomicLong(0L);


    private AtomicBoolean startupFinished = new AtomicBoolean(false);

    private AtomicBoolean finished = new AtomicBoolean(false);

    private AtomicLong nifiJobRepositoryId = new AtomicLong(0L);

    private long startTime;
    final Integer timeNifiIsDownOnStartup = 10;// seconds

    @Test
    public void testBacklog() {


    }


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        flowFileEventProvider = new InMemoryFlowFileEventProvider();
        provenanceEventListener = new ProvenanceEventListener();
        provenanceFeedManager = new ProvenanceFeedManager();
        nifiComponentFlowData = new NifiComponentFlowData();


        provenanceEventListener.setProvenanceFeedManager(provenanceFeedManager);
        provenanceEventListener.setFlowFileEventProvider(flowFileEventProvider);


        nifiComponentFlowData.setNifiRestClient(nifiRestClient);
        provenanceFeedManager.setJobRepository(nifiJobRepository);
        provenanceFeedManager.setNifiComponentFlowData(nifiComponentFlowData);

        provenanceEventReceiver.setProvenanceEventListener(provenanceEventListener);
        provenanceEventReceiver.setProvenanceEventStartupListener(provenanceEventStartupListener);


        provenanceEventStartupListener.setJobRepoApplicationStartupListener(jobRepoApplicationStartupListener);
        provenanceEventStartupListener.setJobRepository(jobRepository);

    }

    /**
     * Mock up the Job Rep
     */
    private void setupMockNifiJobRepository() {
        when(this.nifiJobRepository.createJobInstance(any(NifiJobExecution.class))).thenReturn(nifiJobRepositoryId.getAndIncrement());
        when(this.nifiJobRepository.saveJobExecution(any(NifiJobExecution.class))).thenReturn(nifiJobRepositoryId.getAndIncrement());
        when(this.nifiJobRepository.saveStepExecution(any(FlowFileComponent.class))).thenReturn(nifiJobRepositoryId.getAndIncrement());
        doNothing().when(this.nifiJobRepository).saveJobExecutionContext(any(NifiJobExecution.class), any(Map.class));
        doNothing().when(this.nifiJobRepository).saveStepExecutionContext(any(FlowFileComponent.class), any(Map.class));
        doNothing().when(this.nifiJobRepository).completeStep(any(FlowFileComponent.class));
        doNothing().when(this.nifiJobRepository).completeJobExecution(any(NifiJobExecution.class));

        when(this.nifiJobRepository.findJobExecutionId(any(Long.class), any(String.class))).thenReturn(null);
    }


    private void setupMockNifiRestClient() {
        //  ProcessGroupEntity processGroupEntity = nifiRestClient.getRootProcessGroup();

            doAnswer(getRootProcessGroup()).when(this.nifiRestClient).getRootProcessGroup();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                AboutEntity e = new AboutEntity();
                return e;
            }
        }).when(this.nifiRestClient).getNifiVersion();

        //   Set<ProcessorDTO> processorDTOs = nifiRestClient.getProcessorsForFlow(feedProcessGroupId);
        doAnswer(getProcessorsForFlow()).when(this.nifiRestClient).getProcessorsForFlow(any(String.class));

        NifiComponentFlowData aSpy = Mockito.spy(nifiComponentFlowData);
        doAnswer(getFeedProcessGroup()).when(aSpy).getFeedProcessGroup(any(ProvenanceEventRecordDTO.class));
        doAnswer(isConnectedToNifi(timeNifiIsDownOnStartup)).when(aSpy).isConnectedToNifi();

        provenanceFeedManager.setNifiComponentFlowData(aSpy);


    }


    private Answer getRootProcessGroup() {
        return new Answer<ProcessGroupEntity>() {
            @Override
            public ProcessGroupEntity answer(InvocationOnMock invocationOnMock) throws Throwable {
                ProcessGroupEntity entity = new ProcessGroupEntity();
                ProcessGroupDTO groupDTO = new ProcessGroupDTO();
                groupDTO.setName("Nifi Flow");
                FlowSnippetDTO flowSnippetDTO = new FlowSnippetDTO();
                flowSnippetDTO.setProcessors(newProcessors(10));
                groupDTO.setContents(flowSnippetDTO);
                entity.setProcessGroup(groupDTO);
                return entity;
            }
        };
    }

    private Answer getFeedProcessGroup() {
        return new Answer<ProcessGroupDTO>() {
            @Override
            public ProcessGroupDTO answer(InvocationOnMock invocationOnMock) throws Throwable {
                ProcessGroupDTO groupDTO = new ProcessGroupDTO();
                groupDTO.setName("Nifi Flow");
                FlowSnippetDTO flowSnippetDTO = new FlowSnippetDTO();
                flowSnippetDTO.setProcessors(newProcessors(10));
                groupDTO.setContents(flowSnippetDTO);
                return groupDTO;
            }
        };
    }


    private Answer getProcessorsForFlow() {
        return new Answer<Set<ProcessorDTO>>() {
            @Override
            public Set<ProcessorDTO> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return newProcessors(4);
            }
        };
    }

    private Set<ProcessorDTO> newProcessors(int max) {
        Set<ProcessorDTO> set = new HashSet<>();
        for (int i = 1; i <= max; i++) {
            set.add(newProcessor());
        }
        return set;
    }

    private ProcessorDTO newProcessor() {
        ProcessorDTO dto = new ProcessorDTO();
        dto.setId(UUID.randomUUID().toString());
        dto.setParentGroupId("ROOT");
        dto.setName("Processor " + dto.getId());
        return dto;
    }


    @Test
    public void testJms() {
        this.startTime = System.currentTimeMillis();

        //Simulate the Startup of Pipline Controller by simulating processing of Ops Manager to query for running jobs and get those events
        doAnswer(onStartup(true)).when(this.provenanceEventStartupListener).onStartup(any(DateTime.class));


        //setup Mocks
        setupMockNifiJobRepository();
        setupMockNifiRestClient();
        //print Memory
        heapSize();
        //add the listener for startup
        provenanceEventReceiver.init();


        //process JMS Nifi Events in another thread
        simulateNifiEvents(20, 1000, 1000);
        //startup the application
        this.provenanceEventStartupListener.onStartup(new DateTime());
        //Simulate running events for some time
        while (!finished.get()) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        flowFileEventProvider.sizeOfFlowFileMap();


    }

    private ProvenanceEventDTO newEvent() {
        ProvenanceEventDTO eventDTO = new ProvenanceEventDTO();
        eventDTO.setId(eventId.getAndIncrement() + "");
        eventDTO.setEventId(eventId.get());
        eventDTO.setEventTime(new Date());
        eventDTO.setComponentId(UUID.randomUUID().toString());
        eventDTO.setFlowFileUuid(UUID.randomUUID().toString());
        eventDTO.setEventType("RECEIVE");
        return eventDTO;
    }

    /**
     * Simulate JMS Queue sending in 100 events
     */
    private void simulateNifiEvents(final Integer eventCount, final long startupSleep, final long eventSleep) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < eventCount; i++) {
                    ProvenanceEventDTO event = newEvent();
                    provenanceEventReceiver.receiveTopic(event);
                    //simulate wait when startup is processing backlog
                    if (!startupFinished.get()) {
                        try {
                            Thread.sleep(startupSleep);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (eventSleep > 0L) {
                            try {
                                Thread.sleep(eventSleep);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                finished.set(true);
            }
        });
        thread.start();
    }


    private void processAndWait(long totalTime) throws InterruptedException {
        long start = System.currentTimeMillis();
        long now = System.currentTimeMillis();
        while (now - start < totalTime) {
            Thread.sleep(1000);
            now = System.currentTimeMillis();
        }
        log.info("finished processing");

    }


    private void heapSize() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!finished.get()) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // Get current size of heap in bytes
                    long heapSize = Runtime.getRuntime().totalMemory();

                    // Get maximum size of heap in bytes. The heap cannot grow beyond this size.
                    // Any attempt will result in an OutOfMemoryException.
                    long heapMaxSize = Runtime.getRuntime().maxMemory();

                    // Get amount of free memory within the heap in bytes. This size will increase
                    // after garbage collection and decrease as new objects are created.
                    long heapFreeSize = Runtime.getRuntime().freeMemory();
                    log.info("************** METRICS********* Heap Size {}, Max Size: {}, Free: {} ", heapSize, heapMaxSize, heapFreeSize);
                }
            }
        });
        t.start();

    }

    private Answer onStartup(final boolean isError) {
        return new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                DateTime startTime = (DateTime) invocationOnMock.getArguments()[0];
                System.out.println("Startup" + startTime);
                //Wait 5 seconds
                processAndWait(5000);
                //call the method
                if (isError) {
                    provenanceEventReceiver.onStartupConnectionError();
                }
                provenanceEventReceiver.onEventsInitialized();
                startupFinished.set(true);
                return null;

            }
        };
    }

    private Answer isConnectedToNifi(final Integer timeNifiIsDownOnStartup) {
        return new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                long now = System.currentTimeMillis();
                long diffSeconds = (now - startTime) / 1000;
                if (diffSeconds > timeNifiIsDownOnStartup) {
                    return true;
                } else {
                    return false;
                }

            }
        };
    }


}
