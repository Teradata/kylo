/**
 * 
 */
package com.thinkbiganalytics.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;

import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.core.dataset.InMemoryDatasetProvider;
import com.thinkbiganalytics.metadata.core.feed.InMemoryFeedProvider;
import com.thinkbiganalytics.metadata.core.op.InMemoryDataOperationsProvider;

/**
 *
 * @author Sean Felten
 */
public class MetadataProviderSelectorService extends AbstractControllerService implements MetadataProviderService {

    private static final AllowableValue[] ALLOWABLE_IMPLEMENATIONS = {
            new AllowableValue("MEMORY", "In-memory storage", "An implemenation that stores metadata locally in memory (for development-only)"),
            new AllowableValue("SERVICE", "REST API", "An implemenation that accesses metadata via the metadata service REST API")
    };

    public static final PropertyDescriptor IMPLEMENTATION = new PropertyDescriptor.Builder()
            .name("Implementation")
            .description("Specifies which implementation of the metadata providers should be used")
            .allowableValues(ALLOWABLE_IMPLEMENATIONS)
            .defaultValue("MEMORY")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    // TODO add properties for REST API
    

    private static final List<PropertyDescriptor> properties;

    static {
        final List<PropertyDescriptor> props = new ArrayList<>();
        props.add(IMPLEMENTATION);

        properties = Collections.unmodifiableList(props);
    }


    private volatile FeedProvider feedProvider;
    private volatile DatasetProvider datasetProvider;
    private volatile DataOperationsProvider operationsProvider;
    
    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {
        PropertyValue impl = context.getProperty(IMPLEMENTATION);
        
        if (impl.getValue().equalsIgnoreCase("MEMORY")) {
            this.feedProvider = new InMemoryFeedProvider();
            this.datasetProvider = new InMemoryDatasetProvider();
            this.operationsProvider = new InMemoryDataOperationsProvider();
        } else {
            throw new UnsupportedOperationException("Provider implementations not currently supported: " + impl.getValue());
        }
    }

    @Override
    public FeedProvider getFeedProvider() {
        return this.feedProvider;
    }

    @Override
    public DatasetProvider getDatasetProvider() {
        return this.datasetProvider;
    }

    @Override
    public DataOperationsProvider getDataOperationsProvider() {
        return this.operationsProvider;
    }
    


}
