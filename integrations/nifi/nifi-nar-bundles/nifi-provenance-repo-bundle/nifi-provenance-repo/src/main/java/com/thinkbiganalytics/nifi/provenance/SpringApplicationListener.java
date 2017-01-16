package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.util.SpringApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr186054 on 3/3/16.
 */
public class SpringApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(SpringApplicationListener.class);

    public static Map<String, Object> objectsToAutowire = new HashMap<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("SpringApplicationListener Spring Context refreshed.");
        //spring is now initialized
        for (Map.Entry<String, Object> entry : objectsToAutowire.entrySet()) {
            SpringApplicationContext.getInstance().autowire(entry.getKey(), entry.getValue());
        }
    }


    public static void addObjectToAutowire(String key, Object obj) {
        objectsToAutowire.put(key, obj);
    }


}
