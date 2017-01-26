package com.thinkbiganalytics.ui.rest.controller;

/*-
 * #%L
 * thinkbig-ui-app
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

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

@Api(tags = "configuration")
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

        if (env instanceof AbstractEnvironment) {
            boolean isLocal = (request.getRemoteAddr().equals("127.0.0.1") || request.getRemoteAddr().equals("0:0:0:0:0:0:0:1"));
            properties = StreamSupport.stream(((AbstractEnvironment) env).getPropertySources().spliterator(), false)
                    .filter(source -> source instanceof PropertiesPropertySource)
                    .flatMap(source -> ((PropertiesPropertySource) source).getSource().entrySet().stream())
                    .filter(entry -> isLocal || entry.getKey().startsWith("ui."))
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
