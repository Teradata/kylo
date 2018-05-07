package com.thinkbiganalytics.feedmgr.nifi;

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

import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

/**
 * Service to notify when NiFi goes up or down
 */
@Component
public class NifiConnectionService {

    private static final Logger log = LoggerFactory.getLogger(NifiConnectionService.class);
    private Set<NifiConnectionListener> connectionListeners = new HashSet<>();
    @Inject
    LegacyNifiRestClient nifiRestClient;
    private AtomicBoolean isConnected = new AtomicBoolean(false);

    private ScheduledExecutorService connectionCheckService;

    @PostConstruct
    private void init() {
        initConnectionTimer();
    }

    @PreDestroy
    private void destroy(){
        if(connectionCheckService != null){
            connectionCheckService.shutdown();
        }
    }

    public void subscribeConnectionListener(NifiConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    private void notifyOnConnected() {
        connectionListeners.forEach(NifiConnectionListener::onNiFiConnected);
    }

    private void notifyOnDisconnected() {
        connectionListeners.forEach(NifiConnectionListener::onNiFiDisconnected);
    }

    /**
     * Check to ensure NiFi is up.
     * Notifies the listeners if NiFi changes state
     */
    public void checkConnection() {
        boolean connectionCheck = isConnected();
        if (!isConnected.get() && connectionCheck) {
            if (isConnected.compareAndSet(false, true)) {
                notifyOnConnected();
            }
        } else if (isConnected.get() && !connectionCheck) {
            if (isConnected.compareAndSet(true, false)) {
                notifyOnDisconnected();
            }
        }

    }

    /**
     * @return true if NiFi is connected, false if not
     */
    public boolean isNiFiRunning() {
        return isConnected.get();
    }

    private boolean isConnected() {
        try {
            log.trace("Attempt to check isConnected, getting About entity for {} ", nifiRestClient);
            nifiRestClient.getNiFiRestClient().about();
            log.trace("Successfully connected to Nifi with client {} ", nifiRestClient);
            return true;
        } catch (Exception e) {
            log.trace("Error assessing Nifi: {} ", e);
        }
        return false;
    }


    private void initConnectionTimer() {
        long millis = 5000L; //every 5 seconds check for a connection
        connectionCheckService = Executors.newSingleThreadScheduledExecutor();
        connectionCheckService.scheduleAtFixedRate(this::checkConnection, millis, millis, TimeUnit.MILLISECONDS);
    }


}
