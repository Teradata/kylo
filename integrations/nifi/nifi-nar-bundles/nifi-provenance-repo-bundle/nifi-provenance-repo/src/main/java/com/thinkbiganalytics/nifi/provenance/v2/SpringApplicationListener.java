package com.thinkbiganalytics.nifi.provenance.v2;

import com.thinkbiganalytics.util.SpringApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr186054 on 3/3/16.
 */
public class SpringApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger log = LoggerFactory.getLogger(SpringApplicationListener.class);

    public Map<String, Object> objectsToAutowire = new HashMap<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("SpringApplicationListenerSpringApplicationListenerSpringApplicationListenerSpringApplicationListenerSpringApplicationListenerSpringApplicationListener ");
        //spring is now initialized
        for (Map.Entry<String, Object> entry : objectsToAutowire.entrySet()) {
            autowire(entry.getKey(), entry.getValue());
        }
    }

    public void autowire(String key, Object obj) {
        Object bean = null;
        try {
            bean = SpringApplicationContext.getBean(key);
        } catch (BeansException e) {

        }
        if (bean == null && SpringApplicationContext.getApplicationContext() != null) {
            log.info("AUTOWIRING " + key);
            AutowireCapableBeanFactory autowire = SpringApplicationContext.getApplicationContext().getAutowireCapableBeanFactory();
            autowire.autowireBean(obj);
            //fire PostConstruct methods
            autowire.initializeBean(obj, key);
        }
    }


    public void addObjectToAutowire(String key, Object obj) {
        objectsToAutowire.put(key, obj);
    }

}
