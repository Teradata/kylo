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
import com.thinkbiganalytics.metadata.api.generic.GenericEntity;
import com.thinkbiganalytics.metadata.api.generic.GenericEntityProvider;
import com.thinkbiganalytics.metadata.api.generic.GenericType;
import com.thinkbiganalytics.metadata.api.generic.GenericType.PropertyType;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;

@SpringApplicationConfiguration(classes = { ModeShapeEngineConfig.class })
public class JcrGenericEntityProviderTest extends AbstractTestNGSpringContextTests {

    @Inject
    private GenericEntityProvider provider;
    
    @Inject
    private MetadataAccess metadata;
    

    @Test
    public void testCreateType() {
        String typeName = metadata.commit(new Command<String>() {
            @Override
            public String execute() {
                Map<String, GenericType.PropertyType> fields = new HashMap<>();
                fields.put("name", PropertyType.STRING);
                fields.put("description", PropertyType.STRING);
                fields.put("age", PropertyType.LONG);
                
                GenericType type = provider.createType("Person", fields);
                
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
                GenericType type = provider.getType("Person");
                
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
                List<GenericType> types = provider.getTypes();
                
                return types.size();
            }
        });
        
        assertThat(size).isEqualTo(1);
    }
    
    @Test(dependsOnMethods="testCreateType")
    public void testCreateEntity() {
        GenericEntity.ID id = metadata.commit(new Command<GenericEntity.ID>() {
            @Override
            public GenericEntity.ID execute() {
                GenericType type = provider.getType("Person");
                
                Map<String, Object> props = new HashMap<>();
                props.put("name", "Bob");
                props.put("description", "Silly");
                props.put("age", 50);
                
                GenericEntity entity = provider.createEntity(type, props);
                
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
                List<GenericEntity> list = provider.getEntities();
                
                assertThat(list).isNotNull().hasSize(1);
                
                GenericEntity.ID id = list.get(0).getId();
                GenericEntity entity = provider.getEntity(id);
                
                assertThat(entity).isNotNull();
                assertThat(entity.getProperty("name")).isEqualTo("Bob");
                
                return entity.getTypeName();
            }
        });
        
        assertThat(typeName).isEqualTo("Person");
    }
}
