package com.thinkbiganalytics.install.inspector.inspection;

/*-
 * #%L
 * kylo-install-inspector
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

    private final Logger log = LoggerFactory.getLogger(Configuration.class);
    private static final String VERSION_TXT = "version.txt";
    private static final String APPLICATION_PROPERTIES = "application.properties";
    private static final String SERVICES_SERVICE_APP_SRC_MAIN_RESOURCES = "/services/service-app/src/main/resources/";
    private static final String UI_UI_APP_SRC_MAIN_RESOURCES_APPLICATION_PROPERTIES = "/ui/ui-app/src/main/resources/";
    private static final String KYLO_SERVICES_CONF = "/kylo-services/conf/";
    private static final String KYLO_UI_CONF = "/kylo-ui/conf/";
    private ConfigurableListableBeanFactory servicesFactory;
    private ConfigurableListableBeanFactory uiFactory;
    private Path path;
    private Integer id;
    private String version;
    private String buildDate;

    public Configuration(int id, Path path) {
        this.path = path;
        this.id = id;

        String servicesLocation = path.getUri();
        String uiLocation = path.getUri();
        if (path.isDevMode()) {
            servicesLocation += SERVICES_SERVICE_APP_SRC_MAIN_RESOURCES;
            uiLocation += UI_UI_APP_SRC_MAIN_RESOURCES_APPLICATION_PROPERTIES;
        } else {
            servicesLocation += KYLO_SERVICES_CONF;
            uiLocation += KYLO_UI_CONF;
        }

        uiFactory = createConfiguration(uiLocation + APPLICATION_PROPERTIES);
        servicesFactory = createConfiguration(servicesLocation + APPLICATION_PROPERTIES);

        initBuildProperties(servicesLocation);
    }

    private void initBuildProperties(String servicesLocation) {
        Properties buildProps = new Properties();
        String versionLocation = servicesLocation + VERSION_TXT;
        try {
            buildProps.load(new FileInputStream(versionLocation));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read Kylo version properties from " + versionLocation);
        }
        version = buildProps.getProperty("version");
        buildDate = buildProps.getProperty("build.date");
    }

    private ConfigurableListableBeanFactory createConfiguration(String location) {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        Resource[] resources = new FileSystemResource[]{new FileSystemResource(location)};
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

    private String resolveValue(ConfigurableListableBeanFactory factory, String propertyName) {
        return factory.resolveEmbeddedValue(propertyName);
    }

    String getServicesProperty(String propertyName) {
        return resolveValue(servicesFactory, "${" + propertyName + "}");
    }

    String getUiProperty(String propertyName) {
        return resolveValue(uiFactory, "${" + propertyName + "}");
    }

    public String getVersion() {
        return version;
    }

    public String getBuildDate() {
        return buildDate;
    }
}
