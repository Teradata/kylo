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

import java.io.IOException;

/**
 * Kylo Provenance Event Repository This will intercept NiFi Provenance Events via the KyloRecordWriterDelegate and send them to the KylpProvenanceProcessingQueue with the KyloProvenanceEventConsumer
 * will process in a different thread
 */
public class KyloPersistentProvenanceEventRepository extends PersistentProvenanceRepository {

    private static final Logger log = LoggerFactory.getLogger(KyloPersistentProvenanceEventRepository.class);


    private FeedStatisticsManager feedStatisticsManager;


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
        loadSpring();
        feedStatisticsManager = new FeedStatisticsManager();
        initializeFeedEventStatistics();

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
            KyloRecordWriterDelegate delegate = new KyloRecordWriterDelegate(writers[i], getFeedStatisticsManager());
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
     * Write any feed flow relationships in memory (running flows) to disk
     */
    public final void persistFeedEventStatisticsToDisk() {
        log.info("onShutdown: Attempting to persist any active flow files to disk");
        try {
            //persist running flowfile metadata to disk
            boolean success = FeedEventStatistics.getInstance().backup();
            if (success) {
                log.info("onShutdown: Successfully Finished persisting Kylo Flow processing data to {}", new Object[]{FeedEventStatistics.getInstance().getBackupLocation()});
            } else {
                log.info("onShutdown: FAILED Finished persisting Kylo Flow processing data.");
            }
        } catch (Exception e) {
            //ok to swallow exception here.  this is called when NiFi is shutting down
        }
    }

    @Override
    public synchronized void close() throws IOException {
        super.close();
        persistFeedEventStatisticsToDisk();
    }

    private void initializeFeedEventStatistics() {
        boolean success = FeedEventStatistics.getInstance().loadBackup();
        if (success) {
            log.info("Successfully loaded backup from {} ", FeedEventStatistics.getInstance().getBackupLocation());
        } else {
            log.error("Error loading backup");
        }
    }


    private FeedStatisticsManager getFeedStatisticsManager() {
        return feedStatisticsManager;
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
