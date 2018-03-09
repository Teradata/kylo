package com.thinkbiganalytics.install.inspector.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.install.inspector.inspection.Configuration;
import com.thinkbiganalytics.install.inspector.inspection.DefaultConfiguration;
import com.thinkbiganalytics.install.inspector.inspection.Inspection;
import com.thinkbiganalytics.install.inspector.inspection.InspectionBase;
import com.thinkbiganalytics.install.inspector.inspection.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.PropertySources;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class InspectorService {

    @Value("${info.project.version}")
    private String projectVersion;

    /**
     * This method runs inspections in separate from main application thread.
     * Running inspections in a separate thread is a workaround for a problem where Spring's DataSourceBuilder doesn't
     * take custom classloader set on Spring's Application Context and instead it takes it from current thread.
     * Without this workaround DataSourceBuilder would end up with Inspector App's classloader which doesn't
     * (and is not supposed to) have classes for running inspections.
     */
    @Async
    public Future<Configuration> inspect(Path installPath) throws IOException {
        Configuration config = new DefaultConfiguration(installPath.getUri(), installPath.isDevMode().toString(), projectVersion);

        config.setInspections(Collections.emptyList());

        ClassLoader previousClassloader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(config.getServicesClassloader());

        try {
            AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
            ctx.setClassLoader(config.getServicesClassloader());

            PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
            ppc.setLocation(new FileSystemResource(config.getServicesConfigLocation()));
            ppc.setIgnoreUnresolvablePlaceholders(true);
            ppc.setIgnoreResourceNotFound(false);
            ppc.postProcessBeanFactory(ctx.getBeanFactory());
            ppc.setEnvironment(ctx.getEnvironment());

            PropertySources sources = ppc.getAppliedPropertySources();
            sources.forEach(source -> ctx.getEnvironment().getPropertySources().addLast(source));

            ctx.scan("com.thinkbiganalytics.install.inspector.inspection"
                , "com.thinkbiganalytics.hive.config"
                     // , "com.thinkbiganalytics.server" - this will load the whole kylo-services app
                     // , "com.thinkbiganalytics.kerberos" - this too will scan 'com.thinkbiganalytics'
                     // , "com.thinkbiganalytics.nifi.rest.config"
            );
            ctx.refresh();

            Object service = ctx.getBean("defaultInspectionService");
            Method getInspections = ReflectionUtils.findMethod(service.getClass(), "inspect", String.class, String.class, String.class);
            Object resultJson = ReflectionUtils.invokeMethod(getInspections, service, config.getPath(), config.isDevMode(), projectVersion);
            ObjectMapper mapper = new ObjectMapper();
            List<Inspection> inspections = mapper.readValue(resultJson.toString(), new TypeReference<List<InspectionBase>>() {});
            config.setInspections(inspections);
            return new AsyncResult<>(config);
        } finally {
            Thread.currentThread().setContextClassLoader(previousClassloader);
        }
    }
}
