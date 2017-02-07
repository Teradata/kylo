package com.thinkbiganalytics.metadata.modeshape.generic;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.security.AdminCredentials;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@SpringApplicationConfiguration(classes = { ModeShapeEngineConfig.class, JcrTestConfig.class })
public class JcrExtensibleProvidersTest extends AbstractTestNGSpringContextTests {
    // @formatter:off

    @Inject
    private ExtensibleTypeProvider typeProvider;

    @Inject
    private ExtensibleEntityProvider entityProvider;

    @Inject
    private JcrMetadataAccess metadata;


    @Test
    public void testGetAllDefaultTypes() {
        int size = metadata.commit(new AdminCredentials(), () -> {
            List<ExtensibleType> types = typeProvider.getTypes();

            return types.size();
        });
        // Category + Datasource + DatasourceDefinition + DerivedDatasource + DirectoryDatasource + Feed + FeedSLA + HiveTableDatasource + Metric + Sla + SlaActionConfiguration + SlaCheck + User +
        // UserGroup = 14
        assertThat(size).isEqualTo(14);
    }

    @Test(dependsOnMethods="testGetAllDefaultTypes")
    public void testCreatePersonType() {
        String typeName = metadata.commit(new AdminCredentials(), () ->  {
            ExtensibleType type = typeProvider.buildType("Person")
                            .field("name")
                                .type(FieldDescriptor.Type.STRING)
                                .displayName("Person name")
                                .description("The name of the person")
                                .required(true)
                                .add()
                            .addField("description", FieldDescriptor.Type.STRING)
                            .addField("age", FieldDescriptor.Type.LONG)
                            .build();

            return type.getName();
        });

        assertThat(typeName).isNotNull().isEqualTo("Person");
    }

    @Test(dependsOnMethods="testCreatePersonType")
    public void testCreateEmployeeType() {
        String typeName = metadata.commit(new AdminCredentials(), () -> {
            ExtensibleType person = typeProvider.getType("Person");
            ExtensibleType emp = typeProvider.buildType("Employee")
                            .supertype(person)
                            .field("name")
                                .type(FieldDescriptor.Type.STRING)
                                .displayName("Person name")
                                .description("The name of the person")
                                .required(true)
                                .add()
                            .addField("description", FieldDescriptor.Type.STRING)
                            .addField("age", FieldDescriptor.Type.LONG)
                            .build();

            return emp.getSupertype().getName();
        });

        assertThat(typeName).isNotNull().isEqualTo("Person");
    }

    @Test(dependsOnMethods="testCreatePersonType")
    public void testGetPersonType() {
        final ExtensibleType.ID id = metadata.commit(new AdminCredentials(), () -> {
            ExtensibleType type = typeProvider.getType("Person");

            return type.getId();
        });

        assertThat(id).isNotNull();

        Map<String, FieldDescriptor.Type> fields = metadata.commit(new AdminCredentials(), () -> {
            ExtensibleType type = typeProvider.getType("Person");
            Map<String, FieldDescriptor.Type> map = new HashMap<>();

            for (FieldDescriptor descr : type.getFieldDescriptors()) {
                map.put(descr.getName(), descr.getType());
            }

            return map;
        });

        assertThat(fields).isNotNull();
        assertThat(fields).containsEntry("name", FieldDescriptor.Type.STRING);
        assertThat(fields).containsEntry("description", FieldDescriptor.Type.STRING);
        assertThat(fields).containsEntry("age", FieldDescriptor.Type.LONG);
    }

    @Test(dependsOnMethods="testCreatePersonType")
    public void testGetAllTypes() {
        int size = metadata.commit(new AdminCredentials(), () -> {
            List<ExtensibleType> types = typeProvider.getTypes();

            return types.size();
        });

        // 14 + Person + Employee = 16
        assertThat(size).isEqualTo(16);
    }

    @Test(dependsOnMethods="testCreatePersonType")
    public void testCreateEntity() {
        ExtensibleEntity.ID id = metadata.commit(new AdminCredentials(), () -> {
            ExtensibleType type = typeProvider.getType("Person");

            Map<String, Object> props = new HashMap<>();
            props.put("name", "Bob");
            props.put("description", "Silly");
            props.put("age", 50);

            ExtensibleEntity entity = entityProvider.createEntity(type, props);

            return entity.getId();
        });

        assertThat(id).isNotNull();
    }

    @Test(dependsOnMethods="testCreatePersonType")
    public void testGetEntity() {
        String typeName = metadata.commit(new AdminCredentials(), () ->  {
            List<ExtensibleEntity> list = entityProvider.getEntities();

            assertThat(list).isNotNull().hasSize(1);

            ExtensibleEntity.ID id = list.get(0).getId();
            ExtensibleEntity entity = entityProvider.getEntity(id);

            assertThat(entity).isNotNull();
            assertThat(entity.getProperty("name")).isEqualTo("Bob");

            return entity.getTypeName();
        });

        assertThat(typeName).isEqualTo("Person");
    }
    
    // @formatter:on
}
