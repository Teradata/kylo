package com.thinkbiganalytics.spark.rest;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import com.thinkbiganalytics.spark.rest.model.SimpleResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * Authenticates HTTP requests using HTTP Basic Authentication.
 *
 * <p>Used by Kylo Spark Shell to authenticate requests coming from Kylo Services.</p>
 */
@Component
@Provider
public class BasicAuthenticationRequestFilter implements ContainerRequestFilter {

    @Nullable
    private String authorization;

    @Nullable
    private String password;

    @Nullable
    private String username;

    @Override
    public void filter(@Nonnull final ContainerRequestContext request) {
        // Generate authorization value
        if (authorization == null && username != null && password != null) {
            authorization = "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        }

        // Verify authorization header
        if (authorization != null && !StringUtils.equals(request.getHeaderString(HttpHeaders.AUTHORIZATION), authorization)) {
            final SimpleResponse entity = new SimpleResponse();
            entity.setMessage("Invalid authorization header");
            entity.setStatus(SimpleResponse.Status.ERROR);
            request.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(entity).build());
        }
    }

    @Value("${kylo.client.secret:#{null}}")
    public void setPassword(@Nullable final String value) {
        password = value;
        authorization = null;
    }

    @Value("${kylo.client.id:#{null}}")
    public void setUsername(@Nullable final String value) {
        username = value;
        authorization = null;
    }
}
