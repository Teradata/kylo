package com.thinkbiganalytics.nifi.v2.core.metadata;

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

import com.thinkbiganalytics.metadata.rest.client.MetadataClient;
import com.thinkbiganalytics.nifi.core.api.metadata.KyloNiFiFlowProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.ssl.SSLContextService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;

/**
 *
 */
public class MetadataProviderSelectorService extends AbstractControllerService implements MetadataProviderService {

    public static final PropertyDescriptor CLIENT_URL = new PropertyDescriptor.Builder()
        .name("rest-client-url")
        .displayName("REST Client URL")
        .description("The base URL to the metadata server when the REST API client implementation is chosen.")
        .defaultValue("http://localhost:8400/proxy/v1/metadata")
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
    public static final PropertyDescriptor SSL_CONTEXT_SERVICE = new PropertyDescriptor.Builder()
        .name("SSL Context Service")
        .description("The Controller Service to obtain the SSL Context")
        .required(false)
        .identifiesControllerService(SSLContextService.class)
        .build();
    private static final AllowableValue[] ALLOWABLE_IMPLEMENATIONS = {
        new AllowableValue("LOCAL", "Local, In-memory storage", "An implemenation that stores metadata locally in memory (for development-only)"),
        new AllowableValue("REMOTE", "REST API", "An implementation that accesses metadata via the metadata service REST API")
    };
    public static final PropertyDescriptor IMPLEMENTATION = new PropertyDescriptor.Builder()
        .name("Implementation")
        .description("Specifies which implementation of the metadata providers should be used")
        .allowableValues(ALLOWABLE_IMPLEMENATIONS)
        .defaultValue("REMOTE")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .required(true)
        .build();
    private static final List<PropertyDescriptor> properties;

    static {
        final List<PropertyDescriptor> props = new ArrayList<>();
        props.add(IMPLEMENTATION);
        props.add(CLIENT_URL);
        props.add(CLIENT_USERNAME);
        props.add(CLIENT_PASSWORD);
        props.add(SSL_CONTEXT_SERVICE);
        properties = Collections.unmodifiableList(props);
    }


    private volatile MetadataProvider provider;
    private volatile MetadataRecorder recorder;
    private volatile KyloProvenanceClientProvider kyloProvenanceClientProvider;

    /**
     * The Service holding the SSL Context information
     */
    private SSLContextService sslContextService;

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
            SSLContext sslContext = null;

            if (context.getProperty(SSL_CONTEXT_SERVICE) != null && context.getProperty(SSL_CONTEXT_SERVICE).isSet()) {
                this.sslContextService = context.getProperty(SSL_CONTEXT_SERVICE).asControllerService(SSLContextService.class);
                sslContext = this.sslContextService.createSSLContext(SSLContextService.ClientAuth.REQUIRED);
            }

            if (StringUtils.isEmpty(user)) {
                client = new MetadataClient(uri, sslContext);
            } else {
                client = new MetadataClient(uri, user, password, sslContext);
            }

            this.provider = new MetadataClientProvider(client);
            this.recorder = new MetadataClientRecorder(client);
            this.kyloProvenanceClientProvider = new KyloProvenanceClientProvider(client);
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

    public KyloNiFiFlowProvider getKyloNiFiFlowProvider() {
        return this.kyloProvenanceClientProvider;
    }


    /**
     * Taken from NiFi GetHttp Processor
     */
    private SSLContext createSSLContext(final SSLContextService service)
        throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException, UnrecoverableKeyException {

        final SSLContextBuilder sslContextBuilder = new SSLContextBuilder();

        if (StringUtils.isNotBlank(service.getTrustStoreFile())) {
            final KeyStore truststore = KeyStore.getInstance(service.getTrustStoreType());
            try (final InputStream in = new FileInputStream(new File(service.getTrustStoreFile()))) {
                truststore.load(in, service.getTrustStorePassword().toCharArray());
            }
            sslContextBuilder.loadTrustMaterial(truststore, new TrustSelfSignedStrategy());
        }

        if (StringUtils.isNotBlank(service.getKeyStoreFile())) {
            final KeyStore keystore = KeyStore.getInstance(service.getKeyStoreType());
            try (final InputStream in = new FileInputStream(new File(service.getKeyStoreFile()))) {
                keystore.load(in, service.getKeyStorePassword().toCharArray());
            }
            sslContextBuilder.loadKeyMaterial(keystore, service.getKeyStorePassword().toCharArray());
        }

        sslContextBuilder.useProtocol(service.getSslAlgorithm());

        return sslContextBuilder.build();
    }


}
