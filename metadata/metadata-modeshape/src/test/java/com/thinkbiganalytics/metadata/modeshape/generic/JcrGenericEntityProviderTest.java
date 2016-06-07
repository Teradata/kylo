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
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType.PropertyType;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;

@SpringApplicationConfiguration(classes = { ModeShapeEngineConfig.class })
public class JcrGenericEntityProviderTest extends AbstractTestNGSpringContextTests {

    @Inject
    private ExtensibleEntityProvider provider;
    
    @Inject
    private MetadataAccess metadata;
    

    @Test
    public void testCreateType() {
        String typeName = metadata.commit(new Command<String>() {
            @Override
            public String execute() {
                Map<String, ExtensibleType.PropertyType> fields = new HashMap<>();
                fields.put("name", PropertyType.STRING);
                fields.put("description", PropertyType.STRING);
                fields.put("age", PropertyType.LONG);
                
                ExtensibleType type = provider.createType("Person", fields);
                
                return type.getName();
            }
        });
        
        assertThat(typeName).isNotNull().isEqualTo("Person");
    }
    
    @Test(dependsOnMethods="testCreateType")
    public void getPersonType() {
        boolean found = metadata.commit(new Command<Boolean>() {
            @Override
            public Boolean execute() {
                ExtensibleType type = provider.getType("Person");
                
                return type != null;
            }
        });
        
        assertThat(found).isTrue();
    }
    
    @Test(dependsOnMethods="testCreateType")
    public void getAllType() {
        int size = metadata.commit(new Command<Integer>() {
            @Override
            public Integer execute() {
                List<ExtensibleType> types = provider.getTypes();
                
                return types.size();
            }
        });
        
        assertThat(size).isEqualTo(1);
    }
    
    @Test(dependsOnMethods="testCreateType")
    public void testCreateEntity() {
        ExtensibleEntity.ID id = metadata.commit(new Command<ExtensibleEntity.ID>() {
            @Override
            public ExtensibleEntity.ID execute() {
                ExtensibleType type = provider.getType("Person");
                
                Map<String, Object> props = new HashMap<>();
                props.put("name", "Bob");
                props.put("description", "Silly");
                props.put("age", 50);
                
                ExtensibleEntity entity = provider.createEntity(type, props);
                
                return entity.getId();
            }
        });
        
        assertThat(id).isNotNull();
    }
    
    @Test(dependsOnMethods="testCreateEntity")
    public void testGetEntity() {
        String typeName = metadata.commit(new Command<String>() {
            @Override
            public String execute() {
                List<ExtensibleEntity> list = provider.getEntities();
                
                assertThat(list).isNotNull().hasSize(1);
                
                ExtensibleEntity.ID id = list.get(0).getId();
                ExtensibleEntity entity = provider.getEntity(id);
                
                assertThat(entity).isNotNull();
                assertThat(entity.getProperty("name")).isEqualTo("Bob");
                
                return entity.getTypeName();
            }
        });
        
        assertThat(typeName).isEqualTo("Person");
    }
}
