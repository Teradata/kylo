package com.thinkbiganalytics.nifi.v2.core.cleanup;

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

    /** Property for the Spring context service */
    public static final PropertyDescriptor SPRING_SERVICE = new PropertyDescriptor.Builder()
            .name("Spring Context Service")
            .description("Service for loading a Spring context and providing bean lookup.")
            .identifiesControllerService(SpringContextService.class)
            .required(true)
            .build();

    /** List of property descriptors */
    private static final List<PropertyDescriptor> properties = ImmutableList.of(SPRING_SERVICE);

    /** Spring context service */
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

    @Override
    public void addListener(@Nonnull final String category, @Nonnull final String feedName, @Nonnull final CleanupListener listener) {
        getLogger().debug("Adding cleanup listener: {}.{} - {}", new Object[]{category, feedName, listener});
        springService.getBean(CleanupEventConsumer.class).addListener(category, feedName, listener);
    }

    @Override
    public void removeListener(@Nonnull CleanupListener listener) {
        springService.getBean(CleanupEventConsumer.class).removeListener(listener);
    }
}
