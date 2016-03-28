/**
 * 
 */
package com.thinkbiganalytics.controller.metadata;

import java.net.URI;
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

/**
 *
 * @author Sean Felten
 */
public class MetadataProviderSelectorService extends AbstractControllerService implements MetadataProviderService {

    private static final AllowableValue[] ALLOWABLE_IMPLEMENATIONS = {
            new AllowableValue("LOCAL", "Local, In-memory storage", "An implemenation that stores metadata locally in memory (for development-only)"),
            new AllowableValue("REMOTE", "REST API", "An implemenation that accesses metadata via the metadata service REST API")
    };

    public static final PropertyDescriptor IMPLEMENTATION = new PropertyDescriptor.Builder()
            .name("Implementation")
            .description("Specifies which implementation of the metadata providers should be used")
            .allowableValues(ALLOWABLE_IMPLEMENATIONS)
            .defaultValue("REMOTE")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .required(true)
            .build();
    
    public static final PropertyDescriptor CLIENT_URL = new PropertyDescriptor.Builder()
            .name("rest-client-url")
            .displayName("REST Client URL")
            .description("The base URL to the metadata server when the REST API client implementation is chosen.")
            .defaultValue("http://localhost:8077/api/metadata")
            .addValidator(StandardValidators.URL_VALIDATOR)
            .required(false)
            .build();
    

    private static final List<PropertyDescriptor> properties;

    static {
        final List<PropertyDescriptor> props = new ArrayList<>();
        props.add(IMPLEMENTATION);
        props.add(CLIENT_URL);

        properties = Collections.unmodifiableList(props);
    }


    private volatile MetadataProvider provider;
    
    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {
        PropertyValue impl = context.getProperty(IMPLEMENTATION);
        
        if (impl.getValue().equalsIgnoreCase("REMOTE")) {
            URI uri = URI.create(context.getProperty(IMPLEMENTATION).toString());
            this.provider = new MetadataClientProvider(uri);
        } else {
            throw new UnsupportedOperationException("Provider implementations not currently supported: " + impl.getValue());
        }
    }

    @Override
    public MetadataProvider getProvider() {
        return this.provider;
    }


}
