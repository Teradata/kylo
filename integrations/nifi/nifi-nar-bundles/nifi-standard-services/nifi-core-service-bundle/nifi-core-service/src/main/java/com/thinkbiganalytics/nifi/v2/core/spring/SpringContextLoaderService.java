/*
 * Copyright (c) 2016. Teradata Inc.
 */

/**
 *
 */
package com.thinkbiganalytics.nifi.v2.core.spring;

import com.thinkbiganalytics.nifi.core.api.spring.SpringContextService;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;
import org.springframework.beans.BeansException;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Sean Felten
 */
public class SpringContextLoaderService extends AbstractControllerService implements SpringContextService {

    private volatile AbstractRefreshableConfigApplicationContext context;

    public static final PropertyDescriptor CONFIG_CLASSES = new PropertyDescriptor.Builder()
            .name("Configuraton Classes")
            .description("A comma-separated list of fully qualified names of java config classes")
//            .defaultValue("com.thinkbiganalytics.controller.precond.PreconditionJmsConfiguration")
//            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .required(false)
            .build();

    private static final List<PropertyDescriptor> properties;

    static {
        properties = Collections.unmodifiableList(Arrays.asList(CONFIG_CLASSES));
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @OnEnabled
    public void loadConfiurations(final ConfigurationContext context) throws InitializationException {
        final String classes = context.getProperty(CONFIG_CLASSES).getValue();

        try {
            this.context = new ClassPathXmlApplicationContext();
            this.context.setClassLoader(this.context.getClass().getClassLoader());
            this.context.setConfigLocation("application-context.xml");

//            this.context = new AnnotationConfigApplicationContext();
//            this.context.setClassLoader(this.context.getClass().getClassLoader());
//            
//            for (String name : classes.split(",")) {
//                try {
//                    Class<?> cls = Class.forName(name.trim());
//                    this.context.register(cls);
//                    getLogger().info("Added config class: " + name);
//                } catch (ClassNotFoundException e) {
//                    getLogger().warn("Config class not found: " + name, e);
//                }
//            }
//            
//            this.context.scan("com.thinkbiganalytics");

            getLogger().info("Refreshing spring context");
            this.context.refresh();
            getLogger().info("Sprint context refreshed");
        } catch (BeansException | IllegalStateException e) {
            getLogger().error("Failed to load spring configurations", e);
            throw new InitializationException(e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.SpringContextService#getBean(java.lang.Class)
     */
    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return this.context.getBean(requiredType);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.SpringContextService#getBean(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return this.getBean(name, requiredType);
    }

}
