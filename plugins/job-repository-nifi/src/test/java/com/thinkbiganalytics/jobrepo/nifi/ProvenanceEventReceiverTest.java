package com.thinkbiganalytics.jobrepo.nifi;

import com.thinkbiganalytics.jobrepo.JobRepoApplicationStartupListener;
import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileComponent;
import com.thinkbiganalytics.jobrepo.nifi.model.NifiJobExecution;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.jobrepo.nifi.provenance.*;
import com.thinkbiganalytics.jobrepo.repository.JobRepository;
import com.thinkbiganalytics.jobrepo.repository.dao.NifiJobRepository;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.rest.JerseyClientException;
import javafx.scene.control.Alert;
import org.apache.nifi.web.api.dto.BulletinDTO;
import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

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
    private ProvenanceEventStartupListener provenanceEventStartupListener;

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
    private void setupMockNifiJobRepository(){
        when(this.nifiJobRepository.createJobInstance(any(NifiJobExecution.class))).thenReturn(nifiJobRepositoryId.getAndIncrement());
        when(this.nifiJobRepository.saveJobExecution(any(NifiJobExecution.class))).thenReturn(nifiJobRepositoryId.getAndIncrement());
        when(this.nifiJobRepository.saveStepExecution(any(FlowFileComponent.class))).thenReturn(nifiJobRepositoryId.getAndIncrement());
        doNothing().when(this.nifiJobRepository).saveJobExecutionContext(any(NifiJobExecution.class), any(Map.class));
        doNothing().when(this.nifiJobRepository).saveStepExecutionContext(any(FlowFileComponent.class), any(Map.class));
        doNothing().when(this.nifiJobRepository).completeStep(any(FlowFileComponent.class));
        doNothing().when(this.nifiJobRepository).completeJobExecution(any(NifiJobExecution.class));

    }




    private void setupMockNifiRestClient() {
        //  ProcessGroupEntity processGroupEntity = nifiRestClient.getRootProcessGroup();
        try {
            doAnswer(getRootProcessGroup()).when(this.nifiRestClient).getRootProcessGroup();

        } catch (JerseyClientException e) {
            e.printStackTrace();
        }

        //   Set<ProcessorDTO> processorDTOs = nifiRestClient.getProcessorsForFlow(feedProcessGroupId);
        try {
            doAnswer(getProcessorsForFlow()).when(this.nifiRestClient).getProcessorsForFlow(any(String.class));
        } catch (JerseyClientException e) {
            e.printStackTrace();
        }

        NifiComponentFlowData aSpy = Mockito.spy(nifiComponentFlowData);
        doAnswer(getFeedProcessGroup()).when(aSpy).getFeedProcessGroup(any(ProvenanceEventRecordDTO.class));
        provenanceFeedManager.setNifiComponentFlowData(aSpy);


    }


    private Answer getRootProcessGroup(){
        return  new Answer<ProcessGroupEntity>() {
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

    private Answer getFeedProcessGroup(){
        return  new Answer<ProcessGroupDTO>() {
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




    private Answer getProcessorsForFlow(){
        return  new Answer<Set<ProcessorDTO>>() {
            @Override
            public Set<ProcessorDTO> answer(InvocationOnMock invocationOnMock) throws Throwable {
              return newProcessors(4);
            }
        };
    }
    private Set<ProcessorDTO> newProcessors(int max){
        Set<ProcessorDTO> set = new HashSet<>();
        for(int i=1; i<=max; i++){
            set.add(newProcessor());
        }
        return  set;
    }
    private ProcessorDTO newProcessor(){
        ProcessorDTO dto = new ProcessorDTO();
        dto.setId(UUID.randomUUID().toString());
        dto.setParentGroupId("ROOT");
        dto.setName("Processor "+dto.getId());
        return dto;
    }





    @Test
    public void testJms() {

        //Simulate the Startup of Pipline Controller by simulating processing of Ops Manager to query for running jobs and get those events
        doAnswer(onStartup()).when(this.provenanceEventStartupListener).onStartup(any(DateTime.class));
        //setup Mocks
        setupMockNifiJobRepository();
        setupMockNifiRestClient();
        //print Memory
        heapSize();
        //add the listener for startup
        provenanceEventReceiver.init();


        //process JMS Nifi Events in another thread
        simulateNifiEvents();
        //startup the application
        this.provenanceEventStartupListener.onStartup(new DateTime());
        //Simulate running events for some time
        while(!finished.get()) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        flowFileEventProvider.sizeOfFlowFileMap();


    }

    private  ProvenanceEventDTO newEvent(){
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
    private void simulateNifiEvents(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for( int i=0; i<100; i++) {
                    ProvenanceEventDTO event = newEvent();
                    provenanceEventReceiver.receiveTopic(event);
                    //simulate wait when startup is processing backlog
                    if(!startupFinished.get()) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                finished.set(true);
            }
        });
        thread.start();
    }


    private void  processAndWait(long totalTime) throws InterruptedException {
        long start = System.currentTimeMillis();
        long now = System.currentTimeMillis();
        while(now - start < totalTime){
            Thread.sleep(1000);
            now = System.currentTimeMillis();
        }
        log.info("finished processing");

    }


    private void heapSize(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!finished.get()) {
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

    private Answer onStartup(){
        return  new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                DateTime startTime = (DateTime) invocationOnMock.getArguments()[0];
                System.out.println("Startup"+startTime);
                //Wait 5 seconds
                processAndWait(5000);
                //call the method
                provenanceEventReceiver.onEventsInitialized();
                startupFinished.set(true);
                return null;

            }
        };
    }


}
