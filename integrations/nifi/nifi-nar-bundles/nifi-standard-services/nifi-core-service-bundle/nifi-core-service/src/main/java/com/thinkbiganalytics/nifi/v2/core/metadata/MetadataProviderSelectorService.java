/*
 * Copyright (c) 2016. Teradata Inc.
 */

/**
 *
 */
package com.thinkbiganalytics.nifi.v2.core.metadata;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;

import com.thinkbiganalytics.metadata.rest.client.MetadataClient;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;

/**
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
        .defaultValue("http://localhost:8400/proxy/metadata")
            .addValidator(StandardValidators.URL_VALIDATOR)
            .required(false)
            .build();
    
    public static final PropertyDescriptor CLIENT_USERNAME = new PropertyDescriptor.Builder()
            .name("client-username")
            .displayName("REST Client User Name")
            .description("Optional user name if the client requires a credential")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .defaultValue("dladmin")
            .required(false)
            .build();
    
    public static final PropertyDescriptor CLIENT_PASSWORD = new PropertyDescriptor.Builder()
            .name("client-password")
            .displayName("REST Client Password")
            .description("Optional password if the client requires a credential")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .defaultValue("")
            .sensitive(true)
            .required(false)
            .build();


    private static final List<PropertyDescriptor> properties;

    static {
        final List<PropertyDescriptor> props = new ArrayList<>();
        props.add(IMPLEMENTATION);
        props.add(CLIENT_URL);
        props.add(CLIENT_USERNAME);
        props.add(CLIENT_PASSWORD);
        properties = Collections.unmodifiableList(props);
    }


    private volatile MetadataProvider provider;
    private volatile MetadataRecorder recorder;

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {
        PropertyValue impl = context.getProperty(IMPLEMENTATION);

        if (impl.getValue().equalsIgnoreCase("REMOTE")) {
            URI uri = URI.create(context.getProperty(CLIENT_URL).getValue());
            String user = context.getProperty(CLIENT_USERNAME).getValue();
            String password = context.getProperty(CLIENT_PASSWORD).getValue();
            MetadataClient client;
            
            if (StringUtils.isEmpty(user)) {
                client = new MetadataClient(uri);
            } else {
                client = new MetadataClient(uri, user, password);
            }
            
            this.provider = new MetadataClientProvider(client);
            this.recorder = new MetadataClientRecorder(client);
        } else {
            throw new UnsupportedOperationException("Provider implementations not currently supported: " + impl.getValue());
        }
    }



    @Override
    public MetadataProvider getProvider() {
        return this.provider;
    }
 
    @Override
    public MetadataRecorder getRecorder() {
        return recorder;
    }

}
