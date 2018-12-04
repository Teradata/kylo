package com.thinkbiganalytics.kylo.spark.client.livy;

/*-
 * #%L
 * kylo-spark-livy-server
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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



import com.thinkbiganalytics.kylo.spark.client.LivyClient;
import com.thinkbiganalytics.kylo.spark.client.model.LivyServer;
import com.thinkbiganalytics.kylo.spark.client.model.enums.LivyServerStatus;
import com.thinkbiganalytics.kylo.spark.config.LivyProperties;
import com.thinkbiganalytics.kylo.spark.livy.SparkLivyProcess;
import com.thinkbiganalytics.kylo.spark.livy.SparkLivyProcessManager;
import com.thinkbiganalytics.kylo.spark.model.Session;
import com.thinkbiganalytics.kylo.spark.model.enums.SessionState;
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLHandshakeException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @implNote See: https://carlosbecker.com/posts/exponential-backoff-java8/
 */
public final class LivyHeartbeatMonitor {
    private static final XLogger logger = XLoggerFactory.getXLogger(LivyHeartbeatMonitor.class);

    private int TRIES_UNTIL_NOT_FOUND;
    private int DELAY_CHECK_ON_FAIL; // millis
    private int MAX_DELAY_CHECK_ON_FAIL; // millis
    private int HEARTBEAT_INTERVAL; // heartbeat every second

    final static int THREAD_COUNT = Runtime.getRuntime().availableProcessors() + 1;

    static ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    static Scheduler scheduler = Schedulers.from(executor);

    private final LivyClient livyClient;
    private final JerseyRestClient restClient;

    private LivyServer livyServer;

    private LivyProperties livyProperties;

    /**
     * Constructs a new Livy heartbeat monitor
     */
    public LivyHeartbeatMonitor(LivyClient livyClient,
                                JerseyRestClient restClient,
                                LivyServer livyServer,
                                LivyProperties livyProperties) {
        this.livyClient = livyClient;
        this.restClient = restClient;
        this.livyServer = livyServer;

        this.livyProperties = livyProperties;
        this.DELAY_CHECK_ON_FAIL = livyProperties.getDelayCheckOnFail();
        this.MAX_DELAY_CHECK_ON_FAIL = livyProperties.getMaxDelayCheckOnFail();
        this.TRIES_UNTIL_NOT_FOUND = livyProperties.getTriesUntilNotFound();
        this.HEARTBEAT_INTERVAL = livyProperties.getHeartbeatInterval();
    }

    public void monitorSession(SparkLivyProcess sparkShellProcess) {
        if (livyProperties.isMonitorLivy()) {
            // kick off heart beat
            connectionStatusMap.put(sparkShellProcess, new AtomicBoolean(true));
            checkSessionWithBackoff(sparkShellProcess);
        }
    }

    //private static AtomicBoolean connectionAlive =  new AtomicBoolean(true); // TODO: one per sparkShellProcess
    private static Map<SparkShellProcess, AtomicBoolean> connectionStatusMap = new HashMap<>();

    public Optional<SessionState> checkSession(SparkLivyProcess sparkLivyProcess) {
        // TODO: checkSession seems to get in a state where it is called multiple times per heartbeat
        logger.entry(sparkLivyProcess);

        SessionState sessionState = null;
        try {
            Session session = livyClient.getSession(restClient, sparkLivyProcess);
            sessionState = session.getState();
        } catch (ProcessingException pe) {
            logger.trace("Caught ProcessingException:", pe);
            if (!(pe.getCause() instanceof SocketTimeoutException ||
                  pe.getCause() instanceof SSLHandshakeException ||
                  pe.getCause() instanceof SocketException)) {
                throw logger.throwing(pe);
            }
        } catch (WebApplicationException wae) {
            // recording of state for wae's was taken care of by livyClient.getSession
        }
        return logger.exit(Optional.ofNullable(sessionState));
    }

    public AtomicBoolean serverReady(SparkLivyProcess sparkLivyProcess) {
        Validate.notNull(sparkLivyProcess, "sparkShellProcess cannot be null");
        Optional<SessionState> sessionState = checkSession(sparkLivyProcess);
        AtomicBoolean connectionAlive = connectionStatusMap.get(sparkLivyProcess);
        if (sessionState.isPresent() && livyServer.getLivyServerStatus() == LivyServerStatus.alive) {
            connectionAlive.set(true);
        } else {
            connectionAlive.set(false);
        }
        return connectionAlive;
    }

    // see: https://stackoverflow.com/a/41692001/154461
    public Disposable checkSessionWithBackoff(SparkLivyProcess sparkShellProcess) {
        Validate.notNull(sparkShellProcess, "sparkShellProcess cannot be null");
        logger.entry(sparkShellProcess);

        AtomicInteger generator = new AtomicInteger(0);

        Disposable disposable = Observable.fromCallable(generator::incrementAndGet)
            .repeatWhen(counts -> {
                AtomicInteger retryCounter = new AtomicInteger(0);
                return counts.flatMap(c -> {
                    logger.entry(c);

                    int retry = 0;
                    if (serverReady(sparkShellProcess).get()) {    // what states?   ready / stopped / unknown?  uknown will delay
                        retryCounter.set(0); // reset counter
                        return logger.exit(Observable
                            .timer(HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS)
                            .subscribeOn(scheduler));
                    } else {
                        retry = retryCounter.incrementAndGet();
                        if (retry >= TRIES_UNTIL_NOT_FOUND) {
                            livyServer.setLivyServerStatus(LivyServerStatus.not_found);
                            retryCounter.set(0); // reset counter
                            if (SessionState.FINAL_STATES.contains(livyServer.getLivySessionState(sparkShellProcess.getSessionId()))) {
                                // session is complete so stop monitoring it
                                connectionStatusMap.remove(sparkShellProcess);
                                return logger.exit(Observable.<Long>empty());
                            } else {
                                return logger.exit(Observable   // session alive still, take pulse later...
                                    .timer(HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS)
                                    .subscribeOn(scheduler));
                            }
                        } else {
                            // server down, calculate fail check delay
                            int additionalDelay = Math.min(DELAY_CHECK_ON_FAIL * (int) Math.pow(retry + 1, 2), MAX_DELAY_CHECK_ON_FAIL);
                            logger.trace("retry={}, additionalDelay={}ms", retry, additionalDelay);
                            return logger.exit(Observable
                                .timer(DELAY_CHECK_ON_FAIL
                                       + additionalDelay, TimeUnit.MILLISECONDS)
                                .subscribeOn(scheduler));
                        }
                    }
                });
            })
            .subscribeOn(scheduler)
            .subscribe(numTries -> logger.trace("Running check #{}", numTries))
            ;

        return logger.exit(disposable);
    }


    @PreDestroy
    public void preDestroy() {
        executor.shutdownNow();
    }
}
