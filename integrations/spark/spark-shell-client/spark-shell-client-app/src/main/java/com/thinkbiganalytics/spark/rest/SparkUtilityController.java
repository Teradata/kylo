package com.thinkbiganalytics.spark.rest;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import com.thinkbiganalytics.spark.service.SparkUtilityService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.apache.commons.lang3.Validate;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Utility endpoints for Spark Shell.
 */
@Path("/api/v1/spark/shell")
public class SparkUtilityController {
    /**
     * Spark utility service
     */
    @Context
    private SparkUtilityService sparkUtilityService;


    /**
     * Returns the data sources available to Spark.
     */
    @GET
    @Path("data-sources")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Finds Spark data sources")
    @ApiResponse(code = 200, message = "List of Spark data sources.", response = String.class, responseContainer = "List")
    public Response getDataSources() {
        Validate.notNull(sparkUtilityService); // can be null if not bound to ResourceConfig
        return Response.ok(sparkUtilityService.getDataSources()).build();
    }

}