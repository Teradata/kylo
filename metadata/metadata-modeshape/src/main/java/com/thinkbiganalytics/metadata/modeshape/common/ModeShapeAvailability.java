package com.thinkbiganalytics.metadata.modeshape.common;

import com.thinkbiganalytics.auth.concurrent.ServiceSecurityContextRunnable;

import org.modeshape.jcr.ModeShapeEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Service that classes can subscribe and listen to when ModeShape is up and running.
 */
public class ModeShapeAvailability {

    @Inject
    private ModeShapeEngine modeShapeEngine;

    private Timer modeshapeAvailableTimer;


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

    public boolean isRunning() {
        return (ModeShapeEngine.State.RUNNING.equals(modeShapeEngine.getState()));
    }

    class CheckModeShapeAvailability extends TimerTask {

        private final ServiceSecurityContextRunnable secRunnable = new ServiceSecurityContextRunnable(() -> {
            if (ModeShapeEngine.State.RUNNING.equals(modeShapeEngine.getState())) {
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
