package com.thinkbiganalytics.metadata.modeshape.common;

import com.thinkbiganalytics.auth.concurrent.ServiceSecurityContextRunnable;
import com.thinkbiganalytics.metadata.modeshape.MetadataJcrConfigurator;

import org.modeshape.jcr.ModeShapeEngine;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Service that classes can subscribe and listen to when ModeShape is up and running.
 */
public class ModeShapeAvailability implements ApplicationListener<ContextRefreshedEvent> {

    @Inject
    private ModeShapeEngine modeShapeEngine;
    
    @Inject
    private MetadataJcrConfigurator configurator;

    private Timer modeshapeAvailableTimer;

    private AtomicBoolean applicationStarted = new AtomicBoolean(true);

    private List<ModeShapeAvailabilityListener> listeners = new ArrayList<>();

    @PostConstruct
    public void scheduleServiceLevelAgreements() {
        modeshapeAvailableTimer = new Timer();
        modeshapeAvailableTimer.schedule(new CheckModeShapeAvailability(), 0, 1 * 1000);
    }

    public void subscribe(ModeShapeAvailabilityListener listener) {
        listeners.add(listener);
        if (isRunning()) {
            listener.modeShapeAvailable();
        }
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        applicationStarted.set(true);
    }


    public boolean isRunning() {
        return (ModeShapeEngine.State.RUNNING.equals(modeShapeEngine.getState()) && applicationStarted.get());
    }

    class CheckModeShapeAvailability extends TimerTask {

        private final ServiceSecurityContextRunnable secRunnable = new ServiceSecurityContextRunnable(() -> {
            if (ModeShapeEngine.State.RUNNING.equals(modeShapeEngine.getState()) && configurator.isConfigured() && applicationStarted.get()) {
                modeshapeAvailableTimer.cancel();
                List<ModeShapeAvailabilityListener> currentListeners = Collections.unmodifiableList(listeners);
                for (ModeShapeAvailabilityListener listener : currentListeners) {
                    listener.modeShapeAvailable();
                }
            }
        });

        @Override
        public void run() {
            this.secRunnable.run();
        }
    }

}
