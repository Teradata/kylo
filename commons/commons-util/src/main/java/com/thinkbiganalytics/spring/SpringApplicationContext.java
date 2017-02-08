package com.thinkbiganalytics.spring;

/*-
 * #%L
 * thinkbig-commons-util
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Wrapper to always return a reference to the Spring Application Context from within non-Spring enabled beans.
 */
public class SpringApplicationContext implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(SpringApplicationContext.class);

    private static ApplicationContext CONTEXT;

    public static ApplicationContext getApplicationContext() {
        return CONTEXT;
    }

    /**
     * This method is called from within the ApplicationContext once it is done starting up, it will stick a reference to itself into this bean.
     *
     * @param context a reference to the ApplicationContext.
     */
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        CONTEXT = context;
    }

    /**
     * Return a Spring bean by its name.
     * This will return a generic object, so you must cast it to the correct class.
     * If the bean does not exist, then a Runtime error will be thrown.
     *
     * @param beanName the name of the bean to get.
     * @return an Object reference to the named bean.
     */
    public static Object getBean(String beanName) throws BeansException {
        return CONTEXT.getBean(beanName);
    }

    public static <T> T getBean(Class<T> clazz) {
        return CONTEXT.getBean(clazz);
    }


    /**
     * Autowire properties in the object
     */
    public static Object autowire(String key, Object obj) {

        return autowire(key, obj, true);
    }

    /**
     * Auto wire an object that is created outside of spring but references spring @Autowire annotation
     */
    public static Object autowire(Object obj) {
        autowire(null, obj, true);
        return obj;
    }


    /**
     * Autowire an object Force it to be autowired even if the bean is not registered with the appcontext
     */
    public static Object autowire(String key, Object obj, boolean force) {
        Object bean = null;
        if (key != null) {
            try {
                bean = getBean(key);
            } catch (Exception e) {
                //this is ok
            }
        }
        if (bean == null || force) {

            try {

                if (CONTEXT != null) {
                    AutowireCapableBeanFactory autowire = getApplicationContext().getAutowireCapableBeanFactory();
                    autowire.autowireBean(obj);
                    //fire PostConstruct methods
                    autowire.initializeBean(obj, key);
                    return obj;
                } else {
                    log.error("Unable to autowire {} with Object.  ApplicationContext is null.", key, obj);
                }
            } catch (Exception e) {
                log.error("Unable to autowire {} with Object ", key, obj);
            }
        } else if (bean != null) {
            return bean;
        }
        return null;
    }
}
