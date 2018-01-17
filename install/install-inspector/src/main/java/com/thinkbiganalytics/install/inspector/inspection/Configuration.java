package com.thinkbiganalytics.install.inspector.inspection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.lang.reflect.Field;

public class Configuration {

    private static final String SERVICES_SERVICE_APP_SRC_MAIN_RESOURCES_APPLICATION_PROPERTIES = "/services/service-app/src/main/resources/application.properties";
    private static final String KYLO_SERVICES_CONF_APPLICATION_PROPERTIES = "/kylo-services/conf/application.properties";

    private Path path;
    private Integer id;
    private ConfigurableListableBeanFactory factory;

    public Configuration(int id, Path path) {
        this.path = path;
        this.id = id;

        String location = path.getUri();
        if (path.isDevMode()) {
            location += SERVICES_SERVICE_APP_SRC_MAIN_RESOURCES_APPLICATION_PROPERTIES;
        } else {
            location += KYLO_SERVICES_CONF_APPLICATION_PROPERTIES;
        }

        Resource[] resources = new FileSystemResource[] {new FileSystemResource(location)};
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
        if (properties != null) {
            injectProperties(properties);
        }
        return inspection.inspect(properties);
    }

    private void injectProperties(Object properties) {
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
    }
}
