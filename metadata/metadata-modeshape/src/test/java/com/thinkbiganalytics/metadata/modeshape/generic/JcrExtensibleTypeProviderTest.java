package com.thinkbiganalytics.metadata.modeshape.generic;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;

@SpringApplicationConfiguration(classes = { ModeShapeEngineConfig.class })
public class JcrExtensibleTypeProviderTest extends AbstractTestNGSpringContextTests {

    @Inject
    private ExtensibleTypeProvider typeProvider;
    
    @Inject
    private ExtensibleEntityProvider entityProvider;
    
    @Inject
    private MetadataAccess metadata;
    

    @Test
    public void testGetAllDefaultTypes() {
        int size = metadata.commit(new Command<Integer>() {
            @Override
            public Integer execute() {
                List<ExtensibleType> types = typeProvider.getTypes();
                
                return types.size();
            }
        });
        
        // Feed + FeedConnection + FeedSource + FeedDestination + Datasource = 5
        assertThat(size).isEqualTo(5);
    }

    @Test(dependsOnMethods="testGetAllDefaultTypes")
    public void testCreatePersonType() {
        String typeName = metadata.commit(new Command<String>() {
            @Override
            public String execute() {
                ExtensibleType type = typeProvider.buildType("Person")
                                .field("name")
                                    .type(FieldDescriptor.Type.STRING)
                                    .displayName("Person name")
                                    .description("The name of the person")
                                    .required()
                                    .add()
                                .addField("description", FieldDescriptor.Type.STRING)
                                .addField("age", FieldDescriptor.Type.INTEGER)
                                .build();
                                  
                return type.getName();
            }
        });
        
        assertThat(typeName).isNotNull().isEqualTo("Person");
    }
    
    @Test(dependsOnMethods="testCreatePersonType")
    public void testGetPersonType() {
        boolean found = metadata.commit(new Command<Boolean>() {
            @Override
            public Boolean execute() {
                ExtensibleType type = typeProvider.getType("Person");
                
                return type != null;
            }
        });
        
        assertThat(found).isTrue();
    }
    
    @Test(dependsOnMethods="testCreatePersonType")
    public void testGetAllTypes() {
        int size = metadata.commit(new Command<Integer>() {
            @Override
            public Integer execute() {
                List<ExtensibleType> types = typeProvider.getTypes();
                
                return types.size();
            }
        });
        
        // Person + Feed + FeedConnection + FeedSource + FeedDestination + Datasource = 5
        assertThat(size).isEqualTo(6);
    }
    
    @Test(dependsOnMethods="testCreatePersonType")
    public void testCreateEntity() {
        ExtensibleEntity.ID id = metadata.commit(new Command<ExtensibleEntity.ID>() {
            @Override
            public ExtensibleEntity.ID execute() {
                ExtensibleType type = typeProvider.getType("Person");
                
                Map<String, Object> props = new HashMap<>();
                props.put("name", "Bob");
                props.put("description", "Silly");
                props.put("age", 50);
                
                ExtensibleEntity entity = entityProvider.createEntity(type, props);
                
                return entity.getId();
            }
        });
        
        assertThat(id).isNotNull();
    }
    
    @Test(dependsOnMethods="testCreatePersonType")
    public void testGetEntity() {
        String typeName = metadata.commit(new Command<String>() {
            @Override
            public String execute() {
                List<ExtensibleEntity> list = entityProvider.getEntities();
                
                assertThat(list).isNotNull().hasSize(1);
                
                ExtensibleEntity.ID id = list.get(0).getId();
                ExtensibleEntity entity = entityProvider.getEntity(id);
                
                assertThat(entity).isNotNull();
                assertThat(entity.getProperty("name")).isEqualTo("Bob");
                
                return entity.getTypeName();
            }
        });
        
        assertThat(typeName).isEqualTo("Person");
    }
}
