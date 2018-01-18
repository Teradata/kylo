package com.thinkbiganalytics.install.inspector.inspection;

/*-
 * #%L
 * kylo-install-inspector
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
 * %%
 * %% Licensed under the Apache License, Version 2.0 (the "License");
 * %% you may not use this file except in compliance with the License.
 * %% You may obtain a copy of the License at
 * %%
 * %%     http://www.apache.org/licenses/LICENSE-2.0
 * %%
 * %% Unless required by applicable law or agreed to in writing, software
 * %% distributed under the License is distributed on an "AS IS" BASIS,
 * %% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * %% See the License for the specific language governing permissions and
 * %% limitations under the License.
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.lang.reflect.Field;

public class Configuration {

    private final Logger log = LoggerFactory.getLogger(Configuration.class);

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

    public <SP,UP> InspectionStatus execute(Inspection<SP, UP> inspection) {
        try {
            SP servicesProperties = inspection.getServicesProperties();
            if (servicesProperties != null) {
                autowireProperties(servicesProperties, servicesFactory);
            }
            UP uiProperties = inspection.getUiProperties();
            if (uiProperties != null) {
                autowireProperties(uiProperties, uiFactory);
            }
            return inspection.inspect(servicesProperties, uiProperties);
        } catch (Exception e) {
            String msg = String.format("An error occurred while running configuration inspection '%s'", inspection.getName());
            log.error(msg, e);
            InspectionStatus status = new InspectionStatus(false);
            status.setDescription(msg);
            status.setError(e.getMessage());
            return status;
        }
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
