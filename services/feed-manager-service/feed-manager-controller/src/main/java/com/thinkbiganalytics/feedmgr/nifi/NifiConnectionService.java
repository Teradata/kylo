package com.thinkbiganalytics.feedmgr.nifi;

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
 * Created by sr186054 on 12/21/16.
 */
@Component
public class NifiConnectionService {

    private static final Logger log = LoggerFactory.getLogger(NifiConnectionService.class);

    @Inject
    LegacyNifiRestClient nifiRestClient;

    public Set<NifiConnectionListener> connectionListeners = new HashSet<>();

    private AtomicBoolean isConnected = new AtomicBoolean(false);

    @PostConstruct
    private void init() {
        initConnectionTimer();
    }

    public void subscribeConnectionListener(NifiConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    private void notifyOnConnected() {
        connectionListeners.stream().forEach(nifiConnectionListener -> nifiConnectionListener.onConnected());
    }

    private void notifyOnDisconnected() {
        connectionListeners.stream().forEach(nifiConnectionListener -> nifiConnectionListener.onDisconnected());
    }

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
