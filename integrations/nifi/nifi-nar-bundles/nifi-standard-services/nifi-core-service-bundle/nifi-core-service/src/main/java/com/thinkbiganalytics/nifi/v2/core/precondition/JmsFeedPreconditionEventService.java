package com.thinkbiganalytics.nifi.v2.core.precondition;

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
 * used to communicate precondition events via JMS
 */
public class JmsFeedPreconditionEventService extends AbstractControllerService implements FeedPreconditionEventService {

    /**
     * Property provides the service for loading spring a spring context and providing bean lookup
     */
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

    /**
     * responds to the onConfigured event to wire in the spring context service as a controller service
     *
     * @param context the configuration context of the processor
     * @throws InitializationException if there are any errors getting the spring controller service
     */
    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {
        this.springService = context.getProperty(SPRING_SERVICE).asControllerService(SpringContextService.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.precond.FeedPreconditionEventService#addListener(java.lang.String, com.thinkbiganalytics.controller.precond.PreconditionListener)
     */
    @Override
    public void addListener(String category, String feedName, PreconditionListener listener) {
        PreconditionEventConsumer consumer = this.springService.getBean(PreconditionEventConsumer.class);
        getLogger().debug("Adding preconditon listener: {}.{} - {} to consumer {}", new Object[]{category, feedName, listener, consumer});

        consumer.addListener(category, feedName, listener);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.precond.FeedPreconditionEventService#removeListener(com.thinkbiganalytics.controller.precond.PreconditionListener)
     */
    @Override
    public void removeListener(PreconditionListener listener) {
        PreconditionEventConsumer consumer = this.springService.getBean(PreconditionEventConsumer.class);
        getLogger().debug("Removing preconditon listener: {} from consumer {}", new Object[]{listener, consumer});

        consumer.removeListener(listener);
    }


}
