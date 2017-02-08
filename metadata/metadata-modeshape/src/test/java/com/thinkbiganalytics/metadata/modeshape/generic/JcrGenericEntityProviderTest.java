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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.generic.GenericEntity;
import com.thinkbiganalytics.metadata.api.generic.GenericEntityProvider;
import com.thinkbiganalytics.metadata.api.generic.GenericType;
import com.thinkbiganalytics.metadata.api.generic.GenericType.PropertyType;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@SpringApplicationConfiguration(classes = {ModeShapeEngineConfig.class})
public class JcrGenericEntityProviderTest extends AbstractTestNGSpringContextTests {

    @Inject
    private GenericEntityProvider provider;

    @Inject
    private MetadataAccess metadata;


    @Test(enabled = false)
    public void testCreateType() {
        String typeName = metadata.commit(() -> {
            Map<String, GenericType.PropertyType> fields = new HashMap<>();
            fields.put("name", PropertyType.STRING);
            fields.put("description", PropertyType.STRING);
            fields.put("age", PropertyType.LONG);

            GenericType type = provider.createType("Person", fields);

            return type.getName();
        });

        assertThat(typeName).isNotNull().isEqualTo("Person");
    }

    @Test(enabled = false, dependsOnMethods = "testCreateType")
    public void getPersonType() {
        boolean found = metadata.commit(() -> {
            GenericType type = provider.getType("Person");

            return type != null;
        });

        assertThat(found).isTrue();
    }

    @Test(enabled = false, dependsOnMethods = "testCreateType")
    public void getAllType() {
        int size = metadata.commit(() -> {
            List<GenericType> types = provider.getTypes();

            return types.size();
        });

        assertThat(size).isEqualTo(1);
    }

    @Test(enabled = false, dependsOnMethods = "testCreateType")
    public void testCreateEntity() {
        GenericEntity.ID id = metadata.commit(() -> {
            GenericType type = provider.getType("Person");

            Map<String, Object> props = new HashMap<>();
            props.put("name", "Bob");
            props.put("description", "Silly");
            props.put("age", 50);

            GenericEntity entity = provider.createEntity(type, props);

            return entity.getId();
        });

        assertThat(id).isNotNull();
    }

    @Test(enabled = false, dependsOnMethods = "testCreateEntity")
    public void testGetEntity() {
        String typeName = metadata.commit(() -> {
            List<GenericEntity> list = provider.getEntities();

            assertThat(list).isNotNull().hasSize(1);

            GenericEntity.ID id = list.get(0).getId();
            GenericEntity entity = provider.getEntity(id);

            assertThat(entity).isNotNull();
            assertThat(entity.getProperty("name")).isEqualTo("Bob");

            return entity.getTypeName();
        });

        assertThat(typeName).isEqualTo("Person");
    }
}
