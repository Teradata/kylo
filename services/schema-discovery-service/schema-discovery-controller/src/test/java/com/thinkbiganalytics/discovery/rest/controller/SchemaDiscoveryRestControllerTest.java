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

import com.thinkbiganalytics.discovery.model.DefaultHiveSchema;
import com.thinkbiganalytics.discovery.model.SchemaParserDescriptor;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SchemaDiscoveryRestControllerTest extends JerseyTest {
    
    private static final int PORT = 19998;

    @Override
    protected Application configure() {
        set(TestProperties.CONTAINER_PORT, PORT);
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig config = new ResourceConfig(SchemaDiscoveryRestController.class);
        config.register(MultiPartFeature.class);

        return config;
    }

    @Override
    public void configureClient(ClientConfig config) {
        config.register(MultiPartFeature.class);
    }

    @Test
    public void testUploadFileDirect() throws Exception {
        SchemaParserDescriptor descriptor = createMockParserDescriptor();
        SchemaDiscoveryRestController c = new SchemaDiscoveryRestController();
        Response response = c.uploadFile(ObjectMapperSerializer.serialize(descriptor), new ByteArrayInputStream("ABC".getBytes()), null);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testUploadFile() throws Exception {
        SchemaParserDescriptor descriptor = createMockParserDescriptor();

        FormDataMultiPart form = new FormDataMultiPart();
        form.field("parser", descriptor, MediaType.APPLICATION_JSON_TYPE);
        form.field("fileName", "/Shared/marketing/scrap.txt");
        FormDataBodyPart fdp = new FormDataBodyPart("file",
                                                    new ByteArrayInputStream("ABC".getBytes()), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(fdp);

        Response response = target("/v1/schema-discovery/hive/sample-file").request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(form, form.getMediaType()), Response.class);
        assertEquals(200, response.getStatus());
        assertTrue("Expecting schema object", response.readEntity(DefaultHiveSchema.class) != null);
    }

    @Test
    public void testGetFileParsers() throws Exception {
        final List<SchemaParserDescriptor> list = target("/v1/schema-discovery/file-parsers").request().get(new GenericType<List<SchemaParserDescriptor>>() {
        });
        assertTrue("expecting at least 2 parsers", list.size() >= 2);
    }


    private SchemaParserDescriptor createMockParserDescriptor() {
        SchemaParserDescriptor descriptor = new SchemaParserDescriptor();
        descriptor.setObjectClassType("com.thinkbiganalytics.discovery.rest.controller.MockSchemaParser2");

        FieldRuleProperty propDetect = new FieldRuleProperty();
        propDetect.setName("Auto Detect?");
        propDetect.setObjectProperty("autoDetect");
        propDetect.setValue("true");

        FieldRuleProperty propHeader = new FieldRuleProperty();
        propHeader.setName("Header?");
        propHeader.setObjectProperty("headerRow");
        propHeader.setValue("false");

        descriptor.setProperties(Arrays.asList(propDetect, propHeader));
        return descriptor;
    }
}


