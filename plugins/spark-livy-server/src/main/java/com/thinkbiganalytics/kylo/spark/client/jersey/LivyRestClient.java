package com.thinkbiganalytics.kylo.spark.client.jersey;

/*-
 * #%L
 * kylo-spark-livy-server
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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



import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.spark.conf.model.KerberosSparkProperties;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LivyRestClient extends JerseyRestClient {

    private static final Logger logger = LoggerFactory.getLogger(LivyRestClient.class);

    private CustomConnectorProvider customConnectionProvider;

    private static KerberosSparkProperties kerberosSparkProperties;

    public LivyRestClient(JerseyClientConfig config) {
        super(config);
    }

    public static void setKerberosSparkProperties(KerberosSparkProperties kerberosSparkProperties) {
        LivyRestClient.kerberosSparkProperties = kerberosSparkProperties;
    }

    /**
     * Allows us to modify  the clientConfig to create a cookie jar
     *
     * NOTE: called by super()
     *
     * @param clientConfig the Rest Client Configuration
     */
    @Override
    protected void extendClientConfig(ClientConfig clientConfig) {
        // Use Apache HttpClient for requests..  makes cookie store possible
        customConnectionProvider = new CustomConnectorProvider(kerberosSparkProperties);  // get from Spring instead?
        clientConfig.connectorProvider(customConnectionProvider);

        // turn on buffering or suffer the error `Caused by: org.apache.http.client.NonRepeatableRequestException:
        //     Cannot retry request with a non-repeatable request entity.` when in Keberos mode
        clientConfig.property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED);
    }


    @Override
    protected void registerClientFeatures(Client client) {
        super.registerClientFeatures(client);

        // register our logging filters
        client.register(new LogResponseHeadersFilter());
        client.register(new LogRequestHeadersFilter());
    }

    /**
     * +     * POST an object to a given url +     * +     * @param path the path to access +     * @param headers the request headers +     * @param o    the object to post +     * @return the
     * response +
     */
    public Response postWithHeaders(String path, MultivaluedMap<String, Object> headers, Object o) {
        WebTarget target = buildTarget(path, null);
        return target.request().
                headers(headers).
                post(Entity.entity(o, MediaType.APPLICATION_JSON_TYPE));
    }

    /**
     * Build a target adding the supplied query parameters to the the request
     *
     * @param path   the path to access
     * @param params the key,value parameters to add to the request
     * @return the target to use to make the REST request
     */
    private WebTarget buildTarget(String path, Map<String, Object> params) {
        WebTarget target = getBaseTarget().path(path);
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                target = target.queryParam(entry.getKey(), entry.getValue());
            }
        }
        return target;
    }

    static class LogResponseHeadersFilter implements ClientResponseFilter {

        @Override
        public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
            if (logger.isTraceEnabled()) {
                MultivaluedMap<String, String> headers = responseContext.getHeaders();
                for (String headerName : headers.keySet()) {
                    List<String> headerValues = headers.get(headerName);
                    for (String headerValue : headerValues) {
                        logger.trace("Response Header '{}'='{}'", headerName, headerValue);
                    }
                }
            }
        } // end method
    } // end static class

    static class LogRequestHeadersFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            if (logger.isTraceEnabled()) {
                MultivaluedMap<String, Object> headers = requestContext.getHeaders();
                for (String headerName : headers.keySet()) {
                    List<Object> headerValues = headers.get(headerName);
                    for (Object headerValue : headerValues) {
                        logger.trace("Request Header '{}'='{}'", headerName, headerValue);
                    }
                }
            } // end method
        } // end static class
    }
}
