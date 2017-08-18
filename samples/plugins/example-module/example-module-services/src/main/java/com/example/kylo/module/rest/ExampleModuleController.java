package com.example.kylo.module.rest;

/*-
 * #%L
 * example-module-services
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

import com.google.common.collect.Lists;
import com.thinkbiganalytics.DateTimeUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

@Api(tags = "Example Module")
@Path("/v1/example/module")
@SwaggerDefinition(tags = @Tag(name = "Example Module", description = "Example Module"))
public class ExampleModuleController {


    @Inject
    HttpServletRequest request;

    /**
     * Get the list of foods
     *
     * @return A list of foods
     */
    @GET
    @Path("/food")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets sample foods.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns sample foods.", response = List.class)
                  })
    public Response getFood() {
       List<String> names = Lists.newArrayList("Pizza","Hamburger","Hot dog","Toast","Candy");


        return Response.ok(names).build();
    }

}
