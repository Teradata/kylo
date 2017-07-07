package com.thinkbiganalytics.nifi.v2.core.cleanup;

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

import com.google.common.collect.ImmutableList;
import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupEventConsumer;
import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupEventService;
import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupListener;
import com.thinkbiganalytics.nifi.core.api.spring.SpringContextService;

import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Service that manages the cleanup of feeds.
 */
public class JmsCleanupEventService extends AbstractControllerService implements CleanupEventService {

    /**
     * Property for the Spring context service
     */
    public static final PropertyDescriptor SPRING_SERVICE = new PropertyDescriptor.Builder()
        .name("Spring Context Service")
        .description("Service for loading a Spring context and providing bean lookup.")
        .identifiesControllerService(SpringContextService.class)
        .required(true)
        .build();

    /**
     * List of property descriptors
     */
    private static final List<PropertyDescriptor> properties = ImmutableList.of(SPRING_SERVICE);

    /**
     * Spring context service
     */
    private SpringContextService springService;

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    /**
     * Initializes resources required by this service.
     *
     * @param context the configuration context
     */
    @OnEnabled
    public void onConfigured(@Nonnull final ConfigurationContext context) {
        springService = context.getProperty(SPRING_SERVICE).asControllerService(SpringContextService.class);
    }

    /**
     * adds a listener to be notified on receipt of cleanup events.
     *
     * @param category the category system name
     * @param feedName the feed system name
     * @param listener the listener to be added
     */
    @Override
    public void addListener(@Nonnull final String category, @Nonnull final String feedName, @Nonnull final CleanupListener listener) {
        getLogger().debug("Adding cleanup listener: {}.{} - {}", new Object[]{category, feedName, listener});
        springService.getBean(CleanupEventConsumer.class).addListener(category, feedName, listener);
    }

    /**
     * removes the listener that was previously added with addListener
     *
     * @param listener the listener to be removed
     */
    @Override
    public void removeListener(@Nonnull CleanupListener listener) {
        getLogger().debug("Remove cleanup listener: {}", new Object[]{listener});
        springService.getBean(CleanupEventConsumer.class).removeListener(listener);
    }
}
