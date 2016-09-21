package com.thinkbiganalytics.config.rest.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

@Api(value = "configuration")
@Path("/v1/configuration")
public class ConfigurationController {

    @Inject
    Environment env;

    @Inject
    HttpServletRequest request;

    /**
     * Get the configuration information
     *
     * @return A map of name value key pairs
     */
    @GET
    @Path("/properties")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getConfiguration() {
        final Map<String, Object> properties;

        if ((request.getRemoteAddr().equals("127.0.0.1") || request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) && env instanceof AbstractEnvironment) {
            properties = StreamSupport.stream(((AbstractEnvironment) env).getPropertySources().spliterator(), false)
                    .filter(source -> source instanceof PropertiesPropertySource)
                    .flatMap(source -> ((PropertiesPropertySource) source).getSource().entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            properties = Collections.emptyMap();
        }

        return Response.ok(properties).build();
    }

    @GET
    @Path("/module-urls")
    @Produces({MediaType.APPLICATION_JSON})
    public Response pipelineControllerUrl() {
        final String contextPath = env.getProperty("server.contextPath");
        final String url = StringUtils.isNoneBlank(contextPath) ? contextPath : "";

        final Map<String, String> map = new HashMap<>();
        map.put("opsMgr", url + "/ops-mgr/index.html");
        map.put("feedMgr", url + "/feed-mgr/index.html");
        return Response.ok(map).build();
    }
}
