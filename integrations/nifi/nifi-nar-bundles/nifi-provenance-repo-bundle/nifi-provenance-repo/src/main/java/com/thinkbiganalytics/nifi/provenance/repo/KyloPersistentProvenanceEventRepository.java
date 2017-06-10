package com.thinkbiganalytics.nifi.provenance.repo;

/*-
 * #%L
 * thinkbig-nifi-provenance-repo
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.nifi.provenance.cache.FeedFlowFileMapDbCache;
import com.thinkbiganalytics.nifi.provenance.util.SpringApplicationContext;

import org.apache.nifi.authorization.Authorizer;
import org.apache.nifi.events.EventReporter;
import org.apache.nifi.provenance.PersistentProvenanceRepository;
import org.apache.nifi.provenance.ProvenanceAuthorizableFactory;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.RepositoryConfiguration;
import org.apache.nifi.provenance.serialization.RecordWriter;
import org.apache.nifi.util.NiFiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Kylo Provenance Event Repository
 * This will intercept NiFi Provenance Events via the KyloRecordWriterDelegate and send them to the KylpProvenanceProcessingQueue with the KyloProvenanceEventConsumer will process in a different thread
 */
public class KyloPersistentProvenanceEventRepository extends PersistentProvenanceRepository {

    private static final Logger log = LoggerFactory.getLogger(KyloPersistentProvenanceEventRepository.class);

    /**
     * The shared queue that the Consumer will take in a new thread
     */
   private BlockingQueue<Map.Entry<Long,ProvenanceEventRecord>> processingQueue;

    /**
     * The consumer to take and process the Provenance Events
     */
    KyloProvenanceEventConsumer kyloProvenanceEventConsumer;



    public KyloPersistentProvenanceEventRepository() {
        //  init();
        super();
    }


    public KyloPersistentProvenanceEventRepository(NiFiProperties nifiProperties) throws IOException {
        super(nifiProperties);
        init();
    }

    public KyloPersistentProvenanceEventRepository(RepositoryConfiguration configuration, int rolloverCheckMillis) throws IOException {
        super(configuration, rolloverCheckMillis);
        init();
    }



    private void init() {
        processingQueue = new LinkedBlockingQueue<>();
        loadSpring();
        kyloProvenanceEventConsumer = new KyloProvenanceEventConsumer(processingQueue,false);
       // autowire(kyloProvenanceEventConsumer);
        startConsumer();
    }


    @Override
    public void registerEvent(ProvenanceEventRecord event) {
        super.registerEvent(event);
    }

    @Override
    public void registerEvents(Iterable<ProvenanceEventRecord> events) {
        super.registerEvents(events);
    }

    @Override
    protected RecordWriter[] createWriters(final RepositoryConfiguration config, final long initialRecordId) throws IOException {
        final RecordWriter[] writers = super.createWriters(config, initialRecordId);
        final RecordWriter[] interceptEventIdWriters = new RecordWriter[writers.length];
        for (int i = 0; i < writers.length; i++) {
            KyloRecordWriterDelegate delegate = new KyloRecordWriterDelegate(writers[i], processingQueue);
            interceptEventIdWriters[i] = delegate;
        }
        return interceptEventIdWriters;
    }

    private void loadSpring() {
        try {
            SpringApplicationContext.getInstance().initializeSpring("classpath:provenance-application-context.xml");
        } catch (BeansException | IllegalStateException e) {
            log.error("Failed to load spring configurations", e);
        }
    }

    /**
     * Start the consumer thread to process the events and send to Kylo
     */
    private void startConsumer() {
        initializeFlowFilesFromMapDbCache();
        getTaskExecutor().execute(kyloProvenanceEventConsumer);
    }

    /**
     * Write any feed flow relationships in memory (running flows) to disk
     */
    public final void persistFeedFlowToDisk() {
        log.info("onShutdown: Attempting to persist any active flow files to disk");
        try {
            //persist running flowfile metadata to disk
            int persistedRootFlowFiles = getFlowFileMapDbCache().persistFlowFiles();
            log.info("onShutdown: Finished persisting {} root flow files to disk ", new Object[]{persistedRootFlowFiles});
        } catch (Exception e) {
            //ok to swallow exception here.  this is called when NiFi is shutting down
        }
    }

    @Override
    public synchronized void close() throws IOException {
        super.close();
        persistFeedFlowToDisk();
    }

    private void initializeFlowFilesFromMapDbCache() {
        int loadedRootFlowFiles = getFlowFileMapDbCache().loadGuavaCache();
        log.info("initializeFlowFilesFromMapDbCache: Finished loading {} persisted files from disk into the Guava Cache", new Object[]{loadedRootFlowFiles});
    }


    private TaskExecutor getTaskExecutor() {
        return (ThreadPoolTaskExecutor) SpringApplicationContext.getInstance().getBean("kyloProvenanceProcessingTaskExecutor");
    }


    /**
     * Persistent cache that will only be used when NiFi shuts down or is started, persisting the RootFlowFile objects to help complete Statistics and event processing when NiFi shuts down with events
     * in mid flow processing
     */
    private FeedFlowFileMapDbCache getFlowFileMapDbCache() {
        return SpringApplicationContext.getInstance().getBean(FeedFlowFileMapDbCache.class);
    }

    @Override
    public void initialize(EventReporter eventReporter, Authorizer authorizer, ProvenanceAuthorizableFactory resourceFactory) throws IOException {
        super.initialize(eventReporter, authorizer, resourceFactory);

    }

    /*
   NiFi 1.2 method

    public void initialize(EventReporter eventReporter, Authorizer authorizer, ProvenanceAuthorizableFactory resourceFactory, IdentifierLookup idLookup) throws IOException {
        super.initialize(eventReporter, authorizer, resourceFactory);
    }
    */


}
