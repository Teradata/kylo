package com.thinkbiganalytics.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sr186054 on 3/3/16.
 */
public class SpringInitializer {

    private static class LazyHolder {
        static final SpringInitializer INSTANCE = new SpringInitializer();
    }

    public static SpringInitializer getInstance() {
        return LazyHolder.INSTANCE;
    }


    private AtomicBoolean initialized = new AtomicBoolean(false);

    public void initializeSpring(){
        if(!initialized.get()){
            initialized.set(true);
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {"application-context.xml"});
            System.out.println("INITIALIZED SPRING!!!! "+applicationContext);
        }
    }

}
