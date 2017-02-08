package com.thinkbiganalytics.nifi.security;

/*-
 * #%L
 * thinkbig-nifi-security-api
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

import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.Nonnull;

/**
 * Manages a Spring context for loading security-related classes.
 */
public class SpringSecurityContextLoader {

    /**
     * Spring application context
     */
    @Nonnull
    private final ApplicationContext context;

    /**
     * Constructs a {@code SpringSecurityContextLoader} using the specified Spring application context.
     *
     * @param context the Spring application context
     */
    private SpringSecurityContextLoader(@Nonnull final ApplicationContext context) {
        this.context = context;
    }

    /**
     * Creates a new Security Context Loader from the specified application context.
     *
     * @param applicationContext the application context
     * @return the Security Context Loader
     */
    @Nonnull
    public static SpringSecurityContextLoader create(@Nonnull final ApplicationContext applicationContext) {
        final ClassPathXmlApplicationContext securityContext = new ClassPathXmlApplicationContext(applicationContext);
        securityContext.setConfigLocation("application-context.xml");
        securityContext.refresh();
        return new SpringSecurityContextLoader(securityContext);
    }

    /**
     * Creates a new Security Context Loader from the specified controller service context.
     *
     * @param controllerServiceContext the controller service initialization context
     * @return the Security Context Loader
     */
    @Nonnull
    public static SpringSecurityContextLoader create(@Nonnull final ControllerServiceInitializationContext controllerServiceContext) {
        final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton(ControllerServiceInitializationContext.class.getName(), controllerServiceContext);
        final GenericApplicationContext context = new GenericApplicationContext(beanFactory);
        context.refresh();
        return create(context);
    }

    /**
     * Creates a new Security Context Loader from the specified processor context.
     *
     * @param processorContext the processor initialization context
     * @return the Security Context Loader
     */
    @Nonnull
    public static SpringSecurityContextLoader create(@Nonnull final ProcessorInitializationContext processorContext) {
        final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton(ProcessorInitializationContext.class.getName(), processorContext);
        final GenericApplicationContext context = new GenericApplicationContext(beanFactory);
        context.refresh();
        return create(context);
    }

    /**
     * Gets the common Kerberos properties for controller services and processors.
     *
     * @return the Kerberos properties
     */
    @Nonnull
    public KerberosProperties getKerberosProperties() {
        return context.getBean(KerberosProperties.class);
    }
}
