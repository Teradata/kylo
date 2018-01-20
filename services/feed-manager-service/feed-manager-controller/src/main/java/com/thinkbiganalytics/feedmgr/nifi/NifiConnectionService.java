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

import org.apache.nifi.web.api.dto.AboutDTO;
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

    @PostConstruct
    private void init() {
        initConnectionTimer();
    }

    public void subscribeConnectionListener(NifiConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    private void notifyOnConnected() {
        connectionListeners.stream().forEach(nifiConnectionListener -> nifiConnectionListener.onNiFiConnected());
    }

    private void notifyOnDisconnected() {
        connectionListeners.stream().forEach(nifiConnectionListener -> nifiConnectionListener.onNiFiDisconnected());
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
        return isConnected(false);
    }

    private boolean isConnected(boolean logException) {
        try {
            log.debug("Attempt to check isConnection get about entity for {} ", nifiRestClient);
            AboutDTO aboutEntity = nifiRestClient.getNiFiRestClient().about();
            return aboutEntity != null;
        } catch (Exception e) {
            if (logException) {
                log.error("Error assessing Nifi Connection {} ", e);
            }
        }
        return false;
    }


    private void initConnectionTimer() {
        long millis = 5000L; //every 5 seconds check for a connection
        ScheduledExecutorService service = Executors
            .newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            checkConnection();
        }, millis, millis, TimeUnit.MILLISECONDS);
    }


}
