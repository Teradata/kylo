package com.thinkbiganalytics.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sr186054 on 3/3/16.
 */
public class SpringInitializer {
    private static final Logger log = LoggerFactory.getLogger(SpringInitializer.class);

    private static class LazyHolder {
        static final SpringInitializer INSTANCE = new SpringInitializer();
    }

    public static SpringInitializer getInstance() {
        return LazyHolder.INSTANCE;
    }


    private AtomicBoolean initialized = new AtomicBoolean(false);

    public void initializeSpring() {
        if (!initialized.get()) {
            initialized.set(true);
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[]{"application-context.xml"});
            log.info("INITIALIZED SPRING!!!! " + applicationContext);
        }
    }

}
