/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.rest;

import com.google.common.io.ByteStreams;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.Boundary;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Future;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Generic JerseyRestClient
 *
 * Created by sr186054 on 10/15/15.
 */
public class JerseyRestClient {

    protected static final Logger LOG = LoggerFactory.getLogger(JerseyRestClient.class);

    public static final String HOST_NOT_SET_VALUE = "NOT_SET";

    protected Client client;
    private String uri;
    private String username;

    public boolean isHostConfigured;

    private boolean useConnectionPooling = true;

    public JerseyRestClient(JerseyClientConfig config) {
        useConnectionPooling = config.isUseConnectionPooling();
        SSLContext sslContext = null;
        if (config.isHttps()) {
            SslConfigurator sslConfig = null;
            byte[] keyStoreFile = null;
            byte[] truststoreFile = null;

            try {
                if(StringUtils.isNotBlank(config.getKeystorePath())) {
                InputStream keystore = JerseyRestClient.class.getResourceAsStream(config.getKeystorePath());
                if (keystore != null) {
                    keyStoreFile = ByteStreams.toByteArray(keystore);
                }
                }
            } catch (IOException e) {
            }

            try {
                if(StringUtils.isNotBlank(config.getTruststorePath())) {
                    InputStream truststore = JerseyRestClient.class.getResourceAsStream(config.getTruststorePath());
                    if (truststore != null) {
                        truststoreFile = ByteStreams.toByteArray(truststore);
                    }
                }
            } catch (IOException e) {
            }


            if (keyStoreFile != null) {
           //     LOG.info("Jersey Rest Client using SSL configuration using classpath truststore: {}, keystore: {} ",config.getTruststorePath(), config.getKeystorePath());
                sslConfig = SslConfigurator.newInstance()
                    .trustStoreBytes(truststoreFile != null ? truststoreFile : keyStoreFile)
                    .trustStorePassword(config.getTruststorePassword() != null ? config.getTruststorePassword() : config.getKeystorePassword())
                    .trustStoreType(config.getTrustStoreType())
                    .keyStoreBytes(keyStoreFile != null ? keyStoreFile : truststoreFile)
                    .keyStorePassword(config.getKeystorePassword());
            } else {
              //  LOG.info("Jersey Rest Client using SSL configuration using external truststore: {}, keystore: {} ",config.getTruststorePath(), config.getKeystorePath());
                sslConfig = SslConfigurator.newInstance()
                    .keyStoreFile(config.getKeystorePath() == null ? config.getTruststorePath() : config.getKeystorePath())
                    .keyStorePassword(config.getKeystorePassword() == null ? config.getTruststorePassword() : config.getKeystorePassword())
                    .trustStoreFile(config.getTruststorePath() == null ? config.getKeystorePath() : config.getTruststorePath())
                    .trustStorePassword(config.getTruststorePassword() == null ? config.getKeystorePassword() : config.getTruststorePassword())
                    .trustStoreType(config.getTrustStoreType());
            }

            try {
                sslContext = sslConfig.createSSLContext();
            } catch (Exception e) {
                LOG.error("ERROR creating CLient SSL Context.  " + e.getMessage() + " Falling back to Jersey Client without SSL.  Rest Integration with '"+config.getUrl()+"'  will probably not work until this is fixed!");
               }
        }

        ClientConfig clientConfig = new ClientConfig();

        // Add in Timeouts if configured.  Values are in milliseconds
        if (config.getReadTimeout() != null) {
            clientConfig.property(ClientProperties.READ_TIMEOUT, config.getReadTimeout());
        }
        if (config.getConnectTimeout() != null) {
            clientConfig.property(ClientProperties.CONNECT_TIMEOUT, config.getConnectTimeout());
        }

        if (useConnectionPooling) {

            PoolingHttpClientConnectionManager connectionManager = null;
            if (sslContext != null) {

                HostnameVerifier defaultHostnameVerifier = new DefaultHostnameVerifier();

                LayeredConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    defaultHostnameVerifier);

                final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSocketFactory)
                    .build();

                connectionManager = new PoolingHttpClientConnectionManager(registry);
            } else {
                connectionManager = new PoolingHttpClientConnectionManager();
            }
            connectionManager.setDefaultMaxPerRoute(100); // # of connections allowed per host/address
            connectionManager.setMaxTotal(200); // number of connecttions allowed in total
            // connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost("localhost")), 40);

            clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);
            HttpUrlConnectorProvider connectorProvider = new HttpUrlConnectorProvider();
            clientConfig.connectorProvider(connectorProvider);

        }

        clientConfig.register(MultiPartFeature.class);
        if (sslContext != null) {
            LOG.info("Created new Jersey Client with SSL connecting to {} ", config.getUrl());
            client = new JerseyClientBuilder().withConfig(clientConfig).sslContext(sslContext).build();
        } else {
            LOG.info("Created new Jersey Client without SSL connecting to {} ", config.getUrl());
            client = JerseyClientBuilder.createClient(clientConfig);

        }
        client.register(JacksonFeature.class);
        //  client.register(MultiPartFeature.class);

        if (StringUtils.isNotBlank(config.getUsername())) {
            HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(config.getUsername(), config.getPassword());
            client.register(feature);
        }
        this.uri = config.getUrl();
        this.username = config.getUsername();

        if (StringUtils.isNotBlank(config.getHost()) && !HOST_NOT_SET_VALUE.equals(config.getHost())) {
            this.isHostConfigured = true;
        } else {
            LOG.info("Jersey Rest Client not initialized.  Host name is Not set!!");
        }


    }


    public boolean isHostConfigured() {
        return isHostConfigured;
    }

    public String getUsername() {
        return username;
    }


    /**
     * allow implementers to override to change the base target
     */
    protected WebTarget getBaseTarget() {
        WebTarget target = client.target(uri);
        return target;
    }


    private WebTarget buildTarget(String path, Map<String, Object> params) {
        WebTarget target = getBaseTarget().path(path);
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                target = target.queryParam(entry.getKey(), entry.getValue());

            }
        }
        return target;
    }

    public <T> Future<T> getAsync(String path, Map<String, Object> params, Class<T> clazz) {
        WebTarget target = buildTarget(path, params);
        return target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).async().get(clazz);
    }

    public <T> Future<T> getAsync(String path, Map<String, Object> params, GenericType<T> type) {
        WebTarget target = buildTarget(path, params);
        return target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).async().get(type);

    }

    public <T> T get(String path, Map<String, Object> params, Class<T> clazz) {
        WebTarget target = buildTarget(path, params);
        return target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE).get(clazz);
    }


    public <T> T get(String path, Map<String, Object> params, GenericType<T> type) {
        WebTarget target = buildTarget(path, params);
        return target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE).get(type);
    }

    public Response post(String path, Object o) {
        WebTarget target = buildTarget(path, null);
        return target.request().post(Entity.entity(o, MediaType.APPLICATION_JSON_TYPE));
    }

    public <T> T postMultiPart(String path, MultiPart object, Class<T> returnType) {
        WebTarget target = buildTarget(path, null);
        MediaType contentType = MediaType.MULTIPART_FORM_DATA_TYPE;
        contentType = Boundary.addBoundary(contentType);
        return target.request().post(Entity.entity(object, contentType), returnType);
    }

    public <T> T postMultiPartStream(String path, String name, String fileName, InputStream stream, Class<T> returnType) {
        WebTarget target = buildTarget(path, null);
        MultiPart multiPart = new MultiPart(MediaType.MULTIPART_FORM_DATA_TYPE);

        StreamDataBodyPart streamDataBodyPart = new StreamDataBodyPart(name, stream, fileName, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        multiPart.getBodyParts().add(streamDataBodyPart);
        MediaType contentType = MediaType.MULTIPART_FORM_DATA_TYPE;
        contentType = Boundary.addBoundary(contentType);
        return target.request().post(
            Entity.entity(multiPart, contentType), returnType);
    }


    public <T> T post(String path, Object object, Class<T> returnType) {
        WebTarget target = buildTarget(path, null);
        return target.request().post(Entity.entity(object, MediaType.APPLICATION_JSON), returnType);
    }

    public <T> T put(String path, Object object, Class<T> returnType) {
        WebTarget target = buildTarget(path, null);
        return target.request().put(Entity.entity(object, MediaType.APPLICATION_JSON), returnType);
    }

    public <T> T delete(String path, Map<String, Object> params, Class<T> returnType) {
        WebTarget target = buildTarget(path, params);
        return target.request().delete(returnType);
    }

    public <T> T postForm(String path, Form form, Class<T> returnType) {
        WebTarget target = buildTarget(path, null);
        return target.request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), returnType);
    }

    public <T> Future<T> postAsync(String path, Object object, Class<T> returnType) {
        WebTarget target = buildTarget(path, null);
        return target.request().async().post(Entity.entity(object, MediaType.APPLICATION_JSON), returnType);
    }


}
