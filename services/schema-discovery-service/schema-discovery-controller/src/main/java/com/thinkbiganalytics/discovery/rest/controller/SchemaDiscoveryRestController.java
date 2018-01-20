package com.thinkbiganalytics.discovery.rest.controller;

/*-
 * #%L
 * thinkbig-schema-discovery-controller
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

import com.thinkbiganalytics.discovery.FileParserFactory;
import com.thinkbiganalytics.discovery.model.SchemaParserDescriptor;
import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.policy.PolicyTransformException;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * Provides schema discovery services which is able to generate a schema for file or database sources
 */
@Api(tags = "Feed Manager - Schema Discovery", produces = "application/json")
@Path("/v1/schema-discovery")
@SwaggerDefinition(tags = @Tag(name = "Feed Manager - Schema Discovery", description = "determine file schemas"))
public class SchemaDiscoveryRestController {

    private static final Logger log = LoggerFactory.getLogger(SchemaDiscoveryRestController.class);
    private static final ResourceBundle STRINGS = ResourceBundle.getBundle("com.thinkbiganalytics.discovery.rest.controller.DiscoveryMessages");

    @POST
    @Path("/hive/sample-file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Determines the schema of the provided file.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the schema.", response = Schema.class),
                      @ApiResponse(code = 500, message = "The schema could not be determined.", response = RestResponseStatus.class)
                  })
    public Response uploadFile(@FormDataParam("parser") String parserDescriptor,
                               @FormDataParam("file") InputStream fileInputStream,
                               @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception {

        Schema schema;
        SchemaParserAnnotationTransformer transformer = new SchemaParserAnnotationTransformer();
        try {
            SchemaParserDescriptor descriptor = ObjectMapperSerializer.deserialize(parserDescriptor, SchemaParserDescriptor.class);
            FileSchemaParser p = transformer.fromUiModel(descriptor);
            // TODO: Detect charset
            schema = p.parse(fileInputStream, Charset.defaultCharset(), TableSchemaType.HIVE);
        } catch (IOException e) {
            throw new WebApplicationException(e.getMessage());
        } catch (PolicyTransformException e) {
            log.warn("Failed to convert parser", e);
            throw new InternalServerErrorException(STRINGS.getString("discovery.transformError"), e);
        }
        return Response.ok(schema).build();
    }

    @GET
    @Path("/file-parsers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the available file parsers.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the file parsers.", response = SchemaParserDescriptor.class, responseContainer = "List")
    )
    public Response getFileParsers() {
        List<FileSchemaParser> parsers = FileParserFactory.instance().listSchemaParsers();
        List<SchemaParserDescriptor> descriptors = new ArrayList<>();
        SchemaParserAnnotationTransformer transformer = new SchemaParserAnnotationTransformer();
        for (FileSchemaParser parser : parsers) {
            SchemaParserDescriptor descriptor = transformer.toUIModel(parser);
            descriptors.add(descriptor);
        }
        return Response.ok(descriptors).build();
    }
}
