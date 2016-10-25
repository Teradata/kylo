package com.thinkbiganalytics.nifi.security;

import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Nonnull;

/**
 * Manages a Spring context for loading security-related classes.
 */
public class SpringSecurityContextLoader {

    /**
     * Creates a new Security Context Loader from the specified controller service context.
     *
     * @param controllerServiceContext the controller service initialization context
     * @return the Security Context Loader
     */
    @Nonnull
    public static SpringSecurityContextLoader create(@Nonnull final ControllerServiceInitializationContext controllerServiceContext) {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
        context.setConfigLocation("application-context.xml");
        context.refresh();
        context.getBeanFactory().registerSingleton(ControllerServiceInitializationContext.class.getName(), controllerServiceContext);
        return new SpringSecurityContextLoader(context);
    }

    /**
     * Creates a new Security Context Loader from the specified processor context.
     *
     * @param processorContext the processor initialization context
     * @return the Security Context Loader
     */
    @Nonnull
    public static SpringSecurityContextLoader create(@Nonnull final ProcessorInitializationContext processorContext) {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
        context.setConfigLocation("application-context.xml");
        context.refresh();
        context.getBeanFactory().registerSingleton(ProcessorInitializationContext.class.getName(), processorContext);
        return new SpringSecurityContextLoader(context);
    }

    /** Spring application context */
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
     * Gets the common Kerberos properties for controller services and processors.
     *
     * @return the Kerberos properties
     */
    @Nonnull
    public KerberosProperties getKerberosProperties() {
        return context.getBean(KerberosProperties.class);
    }
}
