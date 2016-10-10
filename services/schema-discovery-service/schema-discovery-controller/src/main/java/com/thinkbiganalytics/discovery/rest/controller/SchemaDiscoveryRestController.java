/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.rest.controller;

import com.thinkbiganalytics.discovery.FileParserFactory;
import com.thinkbiganalytics.discovery.model.DefaultHiveSchema;
import com.thinkbiganalytics.discovery.model.SchemaParserDescriptor;
import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;
import com.thinkbiganalytics.policy.PolicyTransformException;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Provides schema discovery services which is able to generate a schema for file or database sources
 */
@Api(value = "schema-discovery", produces = "application/json")
@Path("/v1/schema-discovery")
public class SchemaDiscoveryRestController {

    private static final Logger log = LoggerFactory.getLogger(SchemaDiscoveryRestController.class);
    private static final ResourceBundle STRINGS = ResourceBundle.getBundle("com.thinkbiganalytics.discovery.rest.controller.DiscoveryMessages");

    public SchemaDiscoveryRestController() {
    }

    @POST
    @Path("/hive/sample-file")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    public Response uploadFile(@FormDataParam("parser") SchemaParserDescriptor parserDescriptor,
                               @FormDataParam("file") InputStream fileInputStream,
                               @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception {

        Schema schema;
        SchemaParserAnnotationTransformer transformer = new SchemaParserAnnotationTransformer();
        try {
            FileSchemaParser p = transformer.fromUiModel(parserDescriptor);
            // TODO: Detect charset
            schema = p.parse(fileInputStream, Charset.defaultCharset(), TableSchemaType.HIVE);
        } catch (PolicyTransformException e) {
            log.warn("Failed to convert parser", e);
            throw new InternalServerErrorException(STRINGS.getString("discovery.transformError"), e);
        }
        return Response.ok(schema).build();
    }

    @GET
    @Path("/file-parsers")
    @Produces({MediaType.APPLICATION_JSON})
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
