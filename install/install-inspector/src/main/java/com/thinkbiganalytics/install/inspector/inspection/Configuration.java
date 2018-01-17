package com.thinkbiganalytics.install.inspector.inspection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.lang.reflect.Field;

public class Configuration {

    private Path path;
    private Integer id;
    private ConfigurableListableBeanFactory factory;

    public Configuration(int id, Path path) {
        this.path = path;
        this.id = id;

        Resource[] resources = new FileSystemResource[] {new FileSystemResource(path.getUri())};
        factory = new DefaultListableBeanFactory();
        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        ppc.setLocations(resources);
        ppc.setIgnoreUnresolvablePlaceholders(true);
        ppc.setSearchSystemEnvironment(false);
        ppc.postProcessBeanFactory(factory);
    }

    public Path getPath() {
        return path;
    }

    public Integer getId() {
        return id;
    }

    public InspectionStatus execute(Inspection inspection) {
        Object properties = inspection.getProperties();
        Field[] fields = properties.getClass().getDeclaredFields();
        for (Field field : fields) {
            Value value = field.getAnnotation(Value.class);
            if (value != null) {
                String propertyName = value.value();
                String propertyValue = factory.resolveEmbeddedValue(propertyName);
                try {
                    field.setAccessible(true);
                    field.set(properties, propertyValue);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(String.format("Failed to initialise inspection property %s.%s", properties.getClass().getSimpleName(), field.getName()));
                }
            }
        }
        return inspection.inspect(properties);
    }
}
