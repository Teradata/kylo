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
import com.thinkbiganalytics.discovery.parser.SampleFileSparkScript;
import com.thinkbiganalytics.discovery.parser.SparkFileSchemaParser;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.policy.PolicyTransformException;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
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

    @Autowired
    private Environment environment;

    /**
     * Generate the spark script that can parse the passed in file using the passed in "parserDescriptor"
     *
     * @param parserDescriptor  metadata about how the file should be parsed
     * @param dataFrameVariable the name of the dataframe variable in the generate spark code
     * @param limit             a number indicating how many rows the script should limit the output
     * @param fileInputStream   the file
     * @param fileMetaData      metadata about the file
     * @return an object including the name of the file on disk and the generated spark script
     */
    @POST
    @Path("/spark/sample-file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Determines the schema of the provided file.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the spark script that parses the sample file.", response = Schema.class),
                      @ApiResponse(code = 500, message = "The schema could not be determined.", response = RestResponseStatus.class)
                  })
    public Response uploadFileSpark(@FormDataParam("parser") String parserDescriptor,
                                    @FormDataParam("dataFrameVariable") @DefaultValue("df") String dataFrameVariable,
                                    @FormDataParam("limit") @DefaultValue("-1") Integer limit,
                                    @FormDataParam("file") InputStream fileInputStream,
                                    @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception {

        SampleFileSparkScript sampleFileSparkScript = null;
        SchemaParserAnnotationTransformer transformer = new SchemaParserAnnotationTransformer();
        try {
            SchemaParserDescriptor descriptor = ObjectMapperSerializer.deserialize(parserDescriptor, SchemaParserDescriptor.class);
            FileSchemaParser p = transformer.fromUiModel(descriptor);
            SparkFileSchemaParser sparkFileSchemaParser = (SparkFileSchemaParser) p;
            sparkFileSchemaParser.setDataFrameVariable(dataFrameVariable);
            sparkFileSchemaParser.setLimit(limit);
            sampleFileSparkScript = sparkFileSchemaParser.getSparkScript(fileInputStream);
        } catch (IOException e) {
            throw new WebApplicationException(e.getMessage());
        } catch (PolicyTransformException e) {
            log.warn("Failed to convert parser", e);
            throw new InternalServerErrorException(STRINGS.getString("discovery.transformError"), e);
        }

        if (sampleFileSparkScript == null) {
            log.warn("Failed to convert parser");
            throw new InternalServerErrorException(STRINGS.getString("discovery.transformError"));
        }
        return Response.ok(sampleFileSparkScript).build();
    }

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

        SchemaParserAnnotationTransformer transformer = new SchemaParserAnnotationTransformer();
        List<SchemaParserDescriptor> list = parsers.stream().map(parser -> transformer.toUIModel(parser))
            .sorted(SchemaParserDescriptorUtil.compareByNameThenPrimaryThenSpark()).collect(Collectors.toList());
        list = SchemaParserDescriptorUtil.keepFirstByName(list);

        return Response.ok(list).build();
    }

    @GET
    @Path("/spark-file-parsers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the available file parsers.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the file parsers.", response = SchemaParserDescriptor.class, responseContainer = "List")
    )
    public Response getSparkFileParsers() {
        List<FileSchemaParser> parsers = FileParserFactory.instance().listSchemaParsers();
        SchemaParserAnnotationTransformer transformer = new SchemaParserAnnotationTransformer();
        List<SchemaParserDescriptor> list = parsers.stream().map(parser -> transformer.toUIModel(parser))
            .sorted(SchemaParserDescriptorUtil.compareByNameThenSpark()).collect(Collectors.toList());
        list = SchemaParserDescriptorUtil.keepFirstByName(list);

        return Response.ok(list).build();
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
