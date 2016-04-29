/*
 * Copyright (c) 2016. Teradata Inc.
 */

/**
 *
 */
package com.thinkbiganalytics.nifi.v2.core.precondition;

import com.thinkbiganalytics.nifi.core.api.precondition.FeedPreconditionEventService;
import com.thinkbiganalytics.nifi.core.api.precondition.PreconditionEventConsumer;
import com.thinkbiganalytics.nifi.core.api.precondition.PreconditionListener;
import com.thinkbiganalytics.nifi.core.api.spring.SpringContextService;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.reporting.InitializationException;

import java.util.Collections;
import java.util.List;

/**
 * @author Sean Felten
 */
public class JmsFeedPreconditionEventService extends AbstractControllerService implements FeedPreconditionEventService {

    public static final PropertyDescriptor SPRING_SERVICE = new PropertyDescriptor.Builder()
            .name("Spring Context Service")
            .description("Service for loading spring a spring context and providing bean lookup")
            .required(true)
            .identifiesControllerService(SpringContextService.class)
            .build();

    private SpringContextService springService;

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return Collections.singletonList(SPRING_SERVICE);
    }

    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {
        this.springService = context.getProperty(SPRING_SERVICE).asControllerService(SpringContextService.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.precond.FeedPreconditionEventService#addListener(java.lang.String, com.thinkbiganalytics.controller.precond.PreconditionListener)
     */
    @Override
    public void addListener(String datasourceName, PreconditionListener listener) {
        getLogger().debug("Adding preconditon listener: {} - {}", new Object[]{datasourceName, listener});
        PreconditionEventConsumer consumer = this.springService.getBean(PreconditionEventConsumer.class);

        consumer.addListener(datasourceName, listener);
        ;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.precond.FeedPreconditionEventService#removeListener(com.thinkbiganalytics.controller.precond.PreconditionListener)
     */
    @Override
    public void removeListener(PreconditionListener listener) {
        PreconditionEventConsumer consumer = this.springService.getBean(PreconditionEventConsumer.class);

        consumer.removeListener(listener);
    }


}
