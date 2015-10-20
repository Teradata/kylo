package com.thinkbiganalytics.rest;

import com.google.common.io.ByteStreams;
import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Generic JerseyRestClient
 *
 * Created by sr186054 on 10/15/15.
 */
public class JerseyRestClient {

    protected static final Logger LOG = LoggerFactory.getLogger(JerseyRestClient.class);


    protected Client client;
    private String uri;
    private String username;

    public JerseyRestClient(JerseyClientConfig config) {
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
                        .trustStorePassword(config.getKeystorePassword());
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
        if(sslContext != null) {
            LOG.info("Created new JIRA Client with SSL");
            client = ClientBuilder.newBuilder().sslContext(sslContext).build();
        } else {
            LOG.info("Created new JIRA Client without SSL");
            client = ClientBuilder.newClient();
        }
        client.register(JacksonFeature.class);
     //   client.register(JodaTimeMapperProvider.class);


        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(config.getUsername(), config.getPassword());
        client.register(feature);
        this.uri = config.getUrl();
        this.username = config.getUsername();


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


    private WebTarget buildTarget(String path, Map<String, String> params) {
        WebTarget target = getBaseTarget().path(path);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                target = target.queryParam(entry.getKey(), entry.getValue());

            }
        }
        return target;
    }

    public <T> Future<T> getAsync(String path, Map<String, String> params, Class<T> clazz) throws JerseyClientException {
        WebTarget target = buildTarget(path, params);
        try {
            return target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).async().get(clazz);
        } catch (ClientErrorException e) {
            String msg = "Get Async Error for " + target.getUri().toString();
            LOG.error(msg, e);
            throw new JerseyClientException(msg, e);
        }
    }

    public <T> Future<T> getAsync(String path, Map<String, String> params, GenericType<T> type) throws JerseyClientException {
        WebTarget target = buildTarget(path, params);
        try {
            return target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).async().get(type);
        } catch (ClientErrorException e) {
            String msg = "Get Async Error for " + target.getUri().toString();
            LOG.error(msg, e);
            throw new JerseyClientException(msg, e);
        }
    }

    public <T> T get(String path, Map<String, String> params, Class<T> clazz) throws JerseyClientException {
        WebTarget target = buildTarget(path, params);
        try {
            return target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(clazz);
        } catch (ClientErrorException e) {
            String msg = "Get Error for " + target.getUri().toString();
            LOG.error(msg, e);
            throw new JerseyClientException(msg, e);
        }
    }

    public <T> T get(String path, Map<String, String> params, GenericType<T> type) throws JerseyClientException {
        WebTarget target = buildTarget(path, params);
        try {
            return target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(type);
        } catch (ClientErrorException e) {
            String msg = "Get Error for " + target.getUri().toString();
            LOG.error(msg, e);
            throw new JerseyClientException(msg, e);
        }
    }


    public Response post(String path, Object o) throws JerseyClientException {
        WebTarget target = buildTarget(path, null);
        try {
            return target.request().post(Entity.entity(o, MediaType.APPLICATION_JSON_TYPE));
        } catch (ClientErrorException e) {
            String msg = "Post Error for " + target.getUri().toString();
            LOG.error(msg, e);
            throw new JerseyClientException(msg, e);
        }
    }

    public <T> T post(String path, Object object, Class<T> returnType) throws JerseyClientException {
        WebTarget target = buildTarget(path, null);
        try {
            return target.request().post(Entity.entity(object, MediaType.APPLICATION_JSON), returnType);
        } catch (ClientErrorException e) {
            String msg = "Post Error for " + target.getUri().toString();
            LOG.error(msg, e);
            throw new JerseyClientException(msg, e);
        }
    }

    public <T> Future<T> postAsync(String path, Object object, Class<T> returnType) throws JerseyClientException {
        WebTarget target = buildTarget(path, null);
        try {
            return target.request().async().post(Entity.entity(object, MediaType.APPLICATION_JSON), returnType);
        } catch (ClientErrorException e) {
            String msg = "Post Async Error for " + target.getUri().toString();
            LOG.error(msg, e);
            throw new JerseyClientException(msg, e);
        }
    }



}
