/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.controller;

import com.thinkbiganalytics.metadata.InMemoryMetadataClientImpl;
import com.thinkbiganalytics.metadata.MetadataClient;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by matthutton on 1/26/16.
 */
public class MetadataConnectionService extends AbstractControllerService implements MetadataService {

    private volatile MetadataClient metadataService;

    public static final PropertyDescriptor REST_URL = new PropertyDescriptor.Builder()
            .name("REST URL")
            .description("The root URL to the REST service.")
            .defaultValue(null)
            .addValidator(StandardValidators.URL_VALIDATOR)
            .required(true)
            .build();

    public static final PropertyDescriptor USER = new PropertyDescriptor.Builder()
            .name("User")
            .description("User")
            .required(true)
            .defaultValue("")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor PASSWORD = new PropertyDescriptor.Builder()
            .name("Password")
            .description("The password")
            .defaultValue(null)
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    private static final List<PropertyDescriptor> properties;

    static {
        final List<PropertyDescriptor> props = new ArrayList<>();
        props.add(REST_URL);
        props.add(USER);
        props.add(PASSWORD);

        properties = Collections.unmodifiableList(props);
    }

    @Override
    public MetadataClient getClient() throws ProcessException {
        return metadataService;
    }


    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    /**
     * Configures connection pool by creating an instance of the
     * {@link BasicDataSource} based on configuration provided with
     * {@link ConfigurationContext}.
     * <p>
     * This operation makes no guarantees that the actual connection could be
     * made since the underlying system may still go off-line during normal
     * operation of the connection pool.
     *
     * @param context the configuration context
     * @throws InitializationException if unable to create a database connection
     */
    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {

        final String url = context.getProperty(REST_URL).getValue();
        final String user = context.getProperty(USER).getValue();
        final String password = context.getProperty(PASSWORD).getValue();

        metadataService = new InMemoryMetadataClientImpl();
    }

    /**
     * Shutdown pool, close all open connections.
     */
    @OnDisabled
    public void shutdown() {
        try {
            // shutdown
        } catch (final Exception e) {
            throw new ProcessException(e);
        }
    }

    @Override
    public String toString() {
        return "MetadataConnectionService[id=" + getIdentifier() + "]";
    }

}
