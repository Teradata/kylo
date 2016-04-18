package com.thinkbiganalytics.spark.rest;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.RuntimeResource;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

/**
 * Adds content origin headers to responses.
 */
public class CorsFilter implements ContainerResponseFilter {
    @Override
    public void filter(@Nonnull final ContainerRequestContext request, @Nonnull final ContainerResponseContext response)
            throws IOException {
        // Allow access from any website
        MultivaluedMap<String, Object> headers = response.getHeaders();
        headers.add("Access-Control-Allow-Origin", "*");

        // Allow access to defined methods
        Set<String> methods = Sets.newHashSet();
        methods.add(HttpMethod.OPTIONS);

        List<RuntimeResource> resources = ((ContainerRequest) request).getUriInfo()
                .getMatchedRuntimeResources();
        if (resources.size() > 0) {
            for (ResourceMethod method : resources.get(0).getResourceMethods()) {
                methods.add(method.getHttpMethod());
            }
        }

        headers.add("Access-Control-Allow-Methods", Joiner.on(',').join(methods));

        // Allow custom content types
        if (methods.contains(HttpMethod.POST)) {
            headers.add("Access-Control-Allow-Headers", "Content-Type");
        }
    }
}
