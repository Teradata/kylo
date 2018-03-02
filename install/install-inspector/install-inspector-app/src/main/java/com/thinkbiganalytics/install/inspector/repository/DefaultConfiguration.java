package com.thinkbiganalytics.install.inspector.repository;

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
import com.thinkbiganalytics.install.inspector.inspection.Configuration;
import com.thinkbiganalytics.install.inspector.inspection.Path;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.PropertySources;
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
    private static final String SERVICES_SERVICE_APP_SRC_MAIN_RESOURCES = "/services/service-app/src/main/resources/";
    private static final String UI_UI_APP_SRC_MAIN_RESOURCES_APPLICATION_PROPERTIES = "/ui/ui-app/src/main/resources/";
    private static final String KYLO_SERVICES_CONF = "/kylo-services/conf/";
    private static final String KYLO_UI_CONF = "/kylo-ui/conf/";

    private final ConfigurableListableBeanFactory servicesFactory;
    private final ConfigurableListableBeanFactory uiFactory;
    private final Path path;
    private final Integer id;
    private final String version;
    private final String buildDate;
    private final String servicesConfigLocation;
    private final ClassLoader servicesClassLoader;
    private final String servicesClasspath;

    public DefaultConfiguration(int id, Path path) {
        this.path = path;
        this.id = id;

        String servicesLocation = path.getUri();
        String uiLocation = path.getUri();

        servicesClasspath = servicesLocation + KYLO_SERVICES_LIB;

        if (path.isDevMode()) {
            servicesLocation += SERVICES_SERVICE_APP_SRC_MAIN_RESOURCES;
            uiLocation += UI_UI_APP_SRC_MAIN_RESOURCES_APPLICATION_PROPERTIES;
        } else {
            servicesLocation += KYLO_SERVICES_CONF;
            uiLocation += KYLO_UI_CONF;
        }

        uiFactory = createConfiguration(uiLocation + APPLICATION_PROPERTIES);
        servicesConfigLocation = servicesLocation + APPLICATION_PROPERTIES;
        servicesFactory = createConfiguration(servicesConfigLocation);

        Properties properties = loadBuildProperties(servicesLocation);
        version = properties.getProperty("version");
        buildDate = properties.getProperty("build.date");

        servicesClassLoader = createClassLoader(servicesClasspath);
    }

    private URLClassLoader createClassLoader(String classpath) {
        File folder = new File(classpath);
        File[] files = folder.listFiles();
        List<URL> jars;
        if (files != null) {
            jars = new ArrayList<>(files.length);
        } else {
            throw new IllegalStateException(String.format("Failed to read classpath '%s'. Is '%s' a valid kylo installation root?", classpath, getPath().getUri()));
        }

        try {
            for (File file : files) {
                jars.add(new URL("jar:file://" + file.getAbsolutePath() + "!/"));
            }
            return new URLClassLoader(jars.toArray(new URL[]{}));
        } catch (MalformedURLException e) {
            throw new IllegalStateException(String.format("Failed to read classpath '%s': %s. Is '%s' a valid kylo installation root?", classpath, e.getMessage(), getPath().getUri()));
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
    public Path getPath() {
        return path;
    }

    @Override
    public Integer getId() {
        return id;
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
    public Object getServicesConfigLocation() {
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
    public <T> T getServicesBean(Class<?> contextConf, Class<T> bean) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();

        PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
        ppc.setLocation(new FileSystemResource(servicesConfigLocation));
        ppc.setIgnoreUnresolvablePlaceholders(true);
        ppc.setIgnoreResourceNotFound(false);
        ppc.postProcessBeanFactory(ctx.getBeanFactory());
        ppc.setEnvironment(ctx.getEnvironment());

        PropertySources sources = ppc.getAppliedPropertySources();
        sources.forEach(source -> ctx.getEnvironment().getPropertySources().addLast(source));

        ctx.register(contextConf);
        ctx.refresh();

        return ctx.getBean(bean);
    }
}
