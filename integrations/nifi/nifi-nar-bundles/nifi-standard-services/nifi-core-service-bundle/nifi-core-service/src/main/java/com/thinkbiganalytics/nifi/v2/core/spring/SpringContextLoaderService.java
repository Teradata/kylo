package com.thinkbiganalytics.nifi.v2.core.spring;

import com.google.common.util.concurrent.SettableFuture;

/*-
 * #%L
 * thinkbig-nifi-core-service
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

import com.thinkbiganalytics.nifi.core.api.spring.SpringContextService;

import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.Validator;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.processor.Processor;
import org.apache.nifi.reporting.InitializationException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Creates a Spring {@link ApplicationContext} that can be reused by other {@link ControllerService} and {@link Processor} objects.
 *
 * <p><b>NOTE:</b> The context is only valid within the NAR file containing this service.</p>
 */
public class SpringContextLoaderService extends AbstractControllerService implements SpringContextService {
    
    /** The maximum number of seconds to wait for bean initialization to finish when retrieving a bean */
    public static final int MAX_BEAN_WAIT_SEC = 60;

    public static final PropertyDescriptor CONFIG_CLASSES = new PropertyDescriptor.Builder()
        .name("Configuraton Classes")
        .description("A comma-separated list of fully qualified names of java config classes")
        .addValidator(Validator.VALID)
        .required(false)
        .build();
    private static final List<PropertyDescriptor> properties;

    static {
        properties = Collections.singletonList(CONFIG_CLASSES);
    }

    private final SettableFuture<AbstractRefreshableConfigApplicationContext> contextFuture = SettableFuture.create();

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    /**
     * Called by the framework to load controller configurations, this method
     * will create a spring context
     *
     * @param context not used in this case
     * @throws InitializationException an except thrown if there are any errors
     */
    @OnEnabled
    public void loadConfiurations(final ConfigurationContext context) throws InitializationException {
        try {
            AbstractRefreshableConfigApplicationContext appContext = new ClassPathXmlApplicationContext();
            appContext.setClassLoader(getClass().getClassLoader());
            appContext.setConfigLocation("application-context.xml");

            getLogger().info("Refreshing spring context");
            appContext.refresh();
            getLogger().info("Spring context refreshed");
            
            this.contextFuture.set(appContext);
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
        return getBean(requiredType.getSimpleName(), cxt -> cxt.getBean(requiredType));
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.spring.SpringContextService#getBean(java.lang.Class, java.lang.Object[])
     */
    @Override
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        return getBean(requiredType.getSimpleName(), cxt -> cxt.getBean(requiredType, args));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.SpringContextService#getBean(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return getBean(name, cxt -> cxt.getBean(name, requiredType));
    }
    
    /**
     * @return true if the Spring context has finished loading for failed to do so
     */
    public boolean isInitialized() {
        return this.contextFuture.isDone();
    }

    private <T> T getBean(String beanName, Function<AbstractRefreshableConfigApplicationContext, T> beanExtractor) {
        try {
            try {
                return beanExtractor.apply(this.contextFuture.get(MAX_BEAN_WAIT_SEC, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                throw new BeanCurrentlyInCreationException(beanName, "Thread interrupted while Spring initialization was in progress");
            } catch (TimeoutException e) {
                throw new BeanCurrentlyInCreationException(beanName, "The requested bean is not yet available as Spring initialization is in progress");
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new BeanInitializationException("Failed to obtain the Spring application context", e);
        }
    }
}
