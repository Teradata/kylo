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
    private static final String UI_UI_APP_SRC_MAIN_RESOURCES_APPLICATION_PROPERTIES = "/ui/ui-app/src/main/resources/application.properties";
    private static final String KYLO_SERVICES_CONF_APPLICATION_PROPERTIES = "/kylo-services/conf/application.properties";
    private static final String KYLO_UI_CONF_APPLICATION_PROPERTIES = "/kylo-ui/conf/application.properties";
    private final ConfigurableListableBeanFactory servicesFactory;
    private final ConfigurableListableBeanFactory uiFactory;
    private final Path path;
    private final Integer id;

    public Configuration(int id, Path path) {
        this.path = path;
        this.id = id;

        String servicesLocation = path.getUri();
        String uiLocation = path.getUri();
        if (path.isDevMode()) {
            servicesLocation += SERVICES_SERVICE_APP_SRC_MAIN_RESOURCES_APPLICATION_PROPERTIES;
            uiLocation += UI_UI_APP_SRC_MAIN_RESOURCES_APPLICATION_PROPERTIES;
        } else {
            servicesLocation += KYLO_SERVICES_CONF_APPLICATION_PROPERTIES;
            uiLocation += KYLO_UI_CONF_APPLICATION_PROPERTIES;
        }

        uiFactory = createConfiguration(uiLocation);
        servicesFactory = createConfiguration(servicesLocation);
    }

    private ConfigurableListableBeanFactory createConfiguration(String location) {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        Resource[] resources = new FileSystemResource[] {new FileSystemResource(location)};
        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        ppc.setLocations(resources);
        ppc.setIgnoreUnresolvablePlaceholders(true);
        ppc.setSearchSystemEnvironment(false);
        ppc.postProcessBeanFactory(factory);
        return factory;
    }

    public Path getPath() {
        return path;
    }

    public Integer getId() {
        return id;
    }

    public InspectionStatus execute(Inspection inspection) {
        Object servicesProperties = inspection.getServicesProperties();
        if (servicesProperties != null) {
            autowireProperties(servicesProperties, servicesFactory);
        }
        Object uiProperties = inspection.getUiProperties();
        if (uiProperties != null) {
            autowireProperties(uiProperties, uiFactory);
        }
        return inspection.inspect(servicesProperties, uiProperties);
    }

    private void autowireProperties(Object properties, ConfigurableListableBeanFactory factory) {
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
