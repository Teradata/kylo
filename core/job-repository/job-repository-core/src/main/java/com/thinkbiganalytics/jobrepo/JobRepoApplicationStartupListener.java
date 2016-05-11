package com.thinkbiganalytics.jobrepo;

import org.joda.time.DateTime;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 5/9/16.
 */
public class JobRepoApplicationStartupListener  implements ApplicationListener<ContextRefreshedEvent> {

    private DateTime startTime = null;
    private List<ApplicationStartupListener> startupListeners = new ArrayList<>();

    public void subscribe(ApplicationStartupListener o) {
            startupListeners.add(o);
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if(startTime == null){
            startTime  = new DateTime();
            for(ApplicationStartupListener startupListener : startupListeners){
                startupListener.onStartup(startTime);
            }
        }
    }
}