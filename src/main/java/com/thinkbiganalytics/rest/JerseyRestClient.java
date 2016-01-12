/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.rest;

import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
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
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.*;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Future;

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
            try {
                InputStream keystore = JerseyRestClient.class.getResourceAsStream(config.getKeystorePath());
                if (keystore != null) {
                    keyStoreFile = ByteStreams.toByteArray(keystore);
                }
            } catch (IOException e) {
            }

            if (keyStoreFile != null) {
                sslConfig = SslConfigurator.newInstance()
                        .trustStoreBytes(keyStoreFile)
                        .trustStorePassword(config.getKeystorePassword())
                .keyStoreBytes(keyStoreFile)
                .keyStorePassword(config.getKeystorePassword());
            } else {
                sslConfig = SslConfigurator.newInstance()
                        .trustStoreFile(config.getKeystorePath())
                        .trustStorePassword(config.getKeystorePassword());
            }

            try {
                 sslContext = sslConfig.createSSLContext();
            }catch(Exception e){
                LOG.error("ERROR creating JiraClient with SSL Context.  "+e.getMessage()+" Falling back to JIRA Client without SSL.  JIRA Integration will probably not work until this is fixed!");
            }
        }

        ClientConfig clientConfig = new ClientConfig();
        // Add in Timeouts if configured.  Values are in milliseconds
        if(config.getReadTimeout() != null) {
            clientConfig.property(ClientProperties.READ_TIMEOUT, config.getReadTimeout());
        }
        if(config.getConnectTimeout() != null) {
            clientConfig.property(ClientProperties.CONNECT_TIMEOUT, config.getConnectTimeout());
        }

        if(useConnectionPooling) {

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
            ApacheConnectorProvider connectorProvider = new ApacheConnectorProvider();
            clientConfig.connectorProvider(connectorProvider);

        }

        if(sslContext != null) {
            LOG.info("Created new JIRA Client with SSL");
            client = ClientBuilder.newBuilder().withConfig(clientConfig).sslContext(sslContext).build();
        } else {
            LOG.info("Created new JIRA Client without SSL");
            client = ClientBuilder.newClient(clientConfig);

        }
        client.register(JacksonFeature.class);

        if(StringUtils.isNotBlank(getUsername())) {
            HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(config.getUsername(), config.getPassword());
            client.register(feature);
        }
        this.uri = config.getUrl();
        this.username = config.getUsername();

        if(StringUtils.isNotBlank(config.getHost()) && ! HOST_NOT_SET_VALUE.equals(config.getHost())) {
            this.isHostConfigured = true;
            LOG.info("Jersey Rest Client initialized");
        }
        else{
            LOG.info("Jersey Rest Client not initialized");
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
     *
     * @return
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

    public <T> Future<T> getAsync(String path, Map<String, Object> params, Class<T> clazz) throws JerseyClientException {
        WebTarget target = buildTarget(path, params);
        try {
            return target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).async().get(clazz);
        } catch (ClientErrorException e) {
            String msg = "Get Async Error for " + target.getUri().toString();
            LOG.error(msg, e);
            throw new JerseyClientException(msg, e);
        }
    }

    public <T> Future<T> getAsync(String path, Map<String, Object> params, GenericType<T> type) throws JerseyClientException {
        WebTarget target = buildTarget(path, params);
        try {
            return target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).async().get(type);
        } catch (ClientErrorException e) {
            String msg = "Get Async Error for " + target.getUri().toString();
            LOG.error(msg);
            throw new JerseyClientException(msg, e);
        }
    }

    public <T> T get(String path, Map<String, Object> params, Class<T> clazz) throws JerseyClientException {
        WebTarget target = buildTarget(path, params);
        try {
            return target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE).get(clazz);
        } catch (Exception e) {
            String msg = "Get Error for " + target.getUri().toString();
            LOG.error(msg);
            throw new JerseyClientException(msg, e);
        }
    }


    public <T> T get(String path, Map<String, Object> params, GenericType<T> type) throws JerseyClientException {
        WebTarget target = buildTarget(path, params);
        try {
            return target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE).get(type);
        } catch (ClientErrorException e) {
            String msg = "Get Error for " + target.getUri().toString();
            LOG.error(msg);
            throw new JerseyClientException(msg, e);
        }
    }


    public Response post(String path, Object o) throws JerseyClientException {
        WebTarget target = buildTarget(path, null);
        try {
            return target.request().post(Entity.entity(o, MediaType.APPLICATION_JSON_TYPE));
        } catch (ClientErrorException e) {
            String msg = "Post Error for " + target.getUri().toString();
            LOG.error(msg);
            throw new JerseyClientException(msg, e);
        }
    }

    public <T> T post(String path, Object object, Class<T> returnType) throws JerseyClientException {
        WebTarget target = buildTarget(path, null);
        try {
            return target.request().post(Entity.entity(object, MediaType.APPLICATION_JSON), returnType);
        } catch (ClientErrorException e) {
            String msg = "Post Error for " + target.getUri().toString();
            LOG.error(msg);
            throw new JerseyClientException(msg, e);
        }
    }

    public <T> T put(String path, Object object, Class<T> returnType) throws JerseyClientException {
        WebTarget target = buildTarget(path, null);
        try {
            return target.request().put(Entity.entity(object, MediaType.APPLICATION_JSON), returnType);
        } catch (ClientErrorException e) {
            String msg = "Put Error for " + target.getUri().toString();
            LOG.error(msg);
            throw new JerseyClientException(msg, e);
        }
    }

    public <T> T postForm(String path, Form form,Class<T> returnType) throws JerseyClientException {
        WebTarget target = buildTarget(path, null);
        try {
         //   Response response = target.request().post(Entity.entity(form,MediaType.APPLICATION_FORM_URLENCODED_TYPE));
          //  return response;
          return  target.request().post(Entity.entity(form,MediaType.APPLICATION_FORM_URLENCODED_TYPE), returnType);
        } catch (ClientErrorException e) {
            String msg = "Post Error for " + target.getUri().toString();
            LOG.error(msg);
            throw new JerseyClientException(msg, e);
        }
    }

    public <T> Future<T> postAsync(String path, Object object, Class<T> returnType) throws JerseyClientException {
        WebTarget target = buildTarget(path, null);
        try {
            return target.request().async().post(Entity.entity(object, MediaType.APPLICATION_JSON), returnType);
        } catch (ClientErrorException e) {
            String msg = "Post Async Error for " + target.getUri().toString();
            LOG.error(msg);
            throw new JerseyClientException(msg, e);
        }
    }




}
