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


import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class DefaultConfiguration implements Configuration {

    private static final String KYLO_SERVICES_LIB = "/kylo-services/lib";
    private static final String VERSION_TXT = "version.txt";
    private static final String APPLICATION_PROPERTIES = "application.properties";
    private static final String SERVICES_SERVICE_APP = "/services/service-app";
    private static final String SERVICES_SERVICE_APP_SRC_MAIN_RESOURCES = SERVICES_SERVICE_APP + "/src/main/resources/";
    private static final String UI_UI_APP_SRC_MAIN_RESOURCES_APPLICATION_PROPERTIES = "/ui/ui-app/src/main/resources/";
    private static final String KYLO_SERVICES_CONF = "/kylo-services/conf/";
    private static final String KYLO_UI_CONF = "/kylo-ui/conf/";

    private final ConfigurableListableBeanFactory servicesFactory;
    private final ConfigurableListableBeanFactory uiFactory;
    private final String version;
    private final String buildDate;
    private final String servicesConfigLocation;
    private final ClassLoader servicesClassLoader;
    private final String servicesClasspath;
    private final String path;
    private final String isDevMode;
    private final String inspectionsPath;
    private List<Inspection> inspections = new ArrayList<>();

    public DefaultConfiguration(String path, String inspectionsPath, String isDevMode, String projectVersion) {
        this.isDevMode = isDevMode;
        this.path = path;

        String servicesLocation = path;
        String uiLocation = path;

        if (Boolean.valueOf(isDevMode)) {
            this.inspectionsPath = servicesLocation + "/install/install-inspector/install-inspector-inspections/target";
            servicesClasspath = servicesLocation + SERVICES_SERVICE_APP + String.format("/target/kylo-service-app-%s-distribution/kylo-service-app-%s/lib", projectVersion, projectVersion);
            servicesLocation += SERVICES_SERVICE_APP_SRC_MAIN_RESOURCES;
            uiLocation += UI_UI_APP_SRC_MAIN_RESOURCES_APPLICATION_PROPERTIES;
        } else {
            servicesClasspath = servicesLocation + KYLO_SERVICES_LIB;
            this.inspectionsPath = inspectionsPath;
            servicesLocation += KYLO_SERVICES_CONF;
            uiLocation += KYLO_UI_CONF;
        }

        try {
            uiFactory = createConfiguration(uiLocation + APPLICATION_PROPERTIES);
            servicesConfigLocation = servicesLocation + APPLICATION_PROPERTIES;
            servicesFactory = createConfiguration(servicesConfigLocation);

            Properties properties = loadBuildProperties(servicesLocation);
            version = properties.getProperty("version");
            buildDate = properties.getProperty("build.date");

            servicesClassLoader = createServicesClassLoader();
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Failed to initialise Kylo installation at path '%s': %s Is '%s' a valid kylo installation root?", getPath(), e.getMessage(), getPath()));
        }
    }

    private URLClassLoader createServicesClassLoader() {
        File folder = new File(servicesClasspath);
        File[] servicesJars = folder.listFiles();
        if (servicesJars == null) {
            throw new IllegalStateException(String.format("Failed to read classpath '%s'. Is '%s' a valid kylo installation root?", servicesClasspath, path));
        }

        folder = new File(inspectionsPath);
        File[] inspectionJars = folder.listFiles();
        if (inspectionJars == null) {
            throw new IllegalStateException(String.format("Failed to read inspections classpath '%s'", inspectionsPath));
        }

        List<File> jarPaths = new ArrayList<>(servicesJars.length + inspectionJars.length);
        jarPaths.addAll(Arrays.asList(servicesJars));
        jarPaths.addAll(Arrays.asList(inspectionJars));

        List<URL> jars = new ArrayList<>(jarPaths.size());
        try {
            for (File jar : jarPaths) {
                jars.add(new URL("jar:file://" + jar.getAbsolutePath() + "!/"));
            }
            return new URLClassLoader(jars.toArray(new URL[]{}));
        } catch (MalformedURLException e) {
            throw new IllegalStateException(String.format("Failed to read classpath '%s': %s. Is '%s' a valid kylo installation root?", servicesClasspath, e.getMessage(), path));
        }
    }

    private Properties loadBuildProperties(String servicesLocation) {
        Properties buildProps = new Properties();
        String versionLocation = servicesLocation + VERSION_TXT;
        try {
            buildProps.load(new FileInputStream(versionLocation));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read Kylo version properties from " + versionLocation);
        }
        return buildProps;
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

    @Override
    public String getPath() {
        return path;
    }

    private String resolveValue(ConfigurableListableBeanFactory factory, String propertyName) {
        return factory.resolveEmbeddedValue(propertyName);
    }

    @Override
    public String getServicesProperty(String propertyName) {
        return resolveValue(servicesFactory, "${" + propertyName + "}");
    }

    @Override
    public String getUiProperty(String propertyName) {
        return resolveValue(uiFactory, "${" + propertyName + "}");
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getBuildDate() {
        return buildDate;
    }

    @Override
    public String getServicesConfigLocation() {
        return servicesConfigLocation;
    }

    @Override
    @JsonIgnore
    public ClassLoader getServicesClassloader() {
        return servicesClassLoader;
    }

    @Override
    public Object getServicesClasspath() {
        return servicesClasspath;
    }

    @Override
    public List<String> getServicesProfiles() {
        String profilesProperty = getServicesProperty(Configuration.SPRING_PROFILES_INCLUDE);
        String[] profiles = profilesProperty.split(",");
        return Arrays.asList(profiles);
    }

    @Override
    public void setInspections(List<Inspection> inspections) {
        this.inspections = inspections;
    }

    @Override
    public List<Inspection> getInspections() {
        return inspections;
    }

    @Override
    public String isDevMode() {
        return isDevMode;
    }
}
