package com.thinkbiganalytics.feedmgr.nifi.cache;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Inspect NiFi collecting all the process groups, processors, connections, etc
 */
public class NiFiFlowInspectorManager {

    private static final Logger log = LoggerFactory.getLogger(NiFiFlowInspectorManager.class);

    private NiFiRestClient restClient;

    private Set<String> processGroupsToInspect = new HashSet<>();

    AtomicLong inspectingCount = new AtomicLong(0);

    private boolean running;

    ExecutorService executorService = null;

    private final CountDownLatch latch = new CountDownLatch(1);

    private Map<String, NiFiFlowInspection> flowsInspected = new ConcurrentHashMap<>();

    private Set<String> processGroupsWithErrors = new HashSet<>();

    private NiFiFlowInspectionCallback completionCallback;

    private String startingProcessGroupId;
    private boolean wait;

    private long totalTime = 0L;
    private DateTime started;
    private DateTime finished;

    private static int DEFAULT_THREADS = 10;
    private static int MAX_THREADS = 100;

    private int threadCount = DEFAULT_THREADS;

    private ThreadFactory flowInspectionThreadFactory = new ThreadFactoryBuilder()
        .setNameFormat("NiFi-Flow-Insepction-%d").build();

    public NiFiFlowInspectorManager(NiFiRestClient restClient) {
        this("root", null, 10, true, restClient);
    }

    public NiFiFlowInspectorManager(NiFiFlowInspectionCallback completionCallback, NiFiRestClient restClient, int numOfThreads, boolean wait) {
        this("root", completionCallback, numOfThreads, wait, restClient);
    }

    public NiFiFlowInspectorManager(NiFiFlowInspectionCallback completionCallback, NiFiRestClient restClient, int numOfThreads) {
        this("root", completionCallback, numOfThreads, false, restClient);
    }

    public NiFiFlowInspectorManager(String startingProcessGroupId, NiFiFlowInspectionCallback completionCallback, int numOfThreads, boolean wait, NiFiRestClient restClient) {
        this.restClient = restClient;

        this.executorService = Executors.newFixedThreadPool(numOfThreads > 0 && numOfThreads < MAX_THREADS ? numOfThreads : DEFAULT_THREADS, flowInspectionThreadFactory);
        this.completionCallback = completionCallback;
        this.startingProcessGroupId = StringUtils.isBlank(startingProcessGroupId) ? "root" : startingProcessGroupId;
        this.wait = wait;
        this.threadCount = numOfThreads;
    }


    public static class NiFiFlowInspectorManagerBuilder {

        private NiFiFlowInspectionCallback completionCallback;
        private String startingProcessGroupId;
        private boolean wait;
        private int threadCount = DEFAULT_THREADS;
        private NiFiRestClient restClient;

        public NiFiFlowInspectorManagerBuilder(NiFiRestClient restClient) {
            this.restClient = restClient;
        }

        public NiFiFlowInspectorManagerBuilder completionCallback(NiFiFlowInspectionCallback callback) {
            this.completionCallback = callback;
            return this;
        }

        public NiFiFlowInspectorManagerBuilder startingProcessGroupId(String startingProcessGroupId) {
            this.startingProcessGroupId = startingProcessGroupId;
            return this;
        }

        public NiFiFlowInspectorManagerBuilder waitUntilComplete(boolean wait) {
            this.wait = wait;
            return this;
        }

        public NiFiFlowInspectorManagerBuilder threads(int threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        public NiFiFlowInspectorManager build() {
            return new NiFiFlowInspectorManager(startingProcessGroupId, completionCallback, threadCount, wait, restClient);
        }

        public NiFiFlowInspectorManager buildAndInspect() {
            NiFiFlowInspectorManager mgr = new NiFiFlowInspectorManager(startingProcessGroupId, completionCallback, threadCount, wait, restClient);
            mgr.inspect();
            return mgr;
        }
    }

    private void reset() {
        flowsInspected.clear();
        running = false;
        inspectingCount.set(0);
        processGroupsToInspect.clear();
    }

    public void addGroupToInspect(String groupId, int level, NiFiFlowInspection parent) {
        int nextLevel = level + 1;
        processGroupsToInspect.add(groupId);
        inspectingCount.incrementAndGet();
        NiFiFlowInspector processGroupInspector = new NiFiFlowInspector(groupId, nextLevel, parent, restClient);
        CompletableFuture<NiFiFlowInspection> flowInspection = CompletableFuture.supplyAsync(() -> processGroupInspector.inspect(), executorService);
        flowInspection.thenAcceptAsync(this::flowInspectionComplete);

    }

    public void addGroupWithError(String processGroupId) {
        processGroupsWithErrors.add(processGroupId);
    }

    private void flowInspectionComplete(NiFiFlowInspection flowInspection) {
        flowInspection.getGroupsToInspect().stream().forEach(processGroupId -> addGroupToInspect(processGroupId, flowInspection.getLevel(), flowInspection));
        processGroupsToInspect.remove(flowInspection.getProcessGroupId());
        inspectingCount.decrementAndGet();
        flowsInspected.put(flowInspection.getProcessGroupId(), flowInspection);
        if (!flowInspection.isComplete()) {
            addGroupWithError(flowInspection.getProcessGroupId());
            //short circuit and stop the inspection
            completeInspection();
        }

        if (isFinished()) {
            completeInspection();
        }
    }

    private void completeInspection() {
        if (hasErrors()) {
            log.error("Errors were found while inspecting NiFi process groups.");
        } else {
            log.info("Completed NiFi inspection of process groups");
        }
        running = false;
        finished = DateTime.now();
        totalTime = finished.getMillis() - started.getMillis();
        if (completionCallback != null && !hasErrors()) {
            completionCallback.execute(this);
        }
        if (wait) {
            latch.countDown();
        }
        executorService.shutdown();
    }

    public boolean hasErrors() {
        return !processGroupsWithErrors.isEmpty();
    }

    public boolean isFinished() {
        return inspectingCount.get() == 0;
    }

    /**
     * Start inspecting NiFi
     */
    public void inspect() {
        reset();
        running = true;
        started = DateTime.now();
        addGroupToInspect(startingProcessGroupId, 0, null);
        if (wait) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.error("Error waiting for latch", e);
            }
        }


    }

    public long getTotalTime() {
        return totalTime;
    }

    public DateTime getStarted() {
        return started;
    }

    public DateTime getFinished() {
        return finished;
    }

    public Map<String, NiFiFlowInspection> getFlowsInspected() {
        return flowsInspected;
    }

    public int getThreadCount() {
        return threadCount;
    }
}
