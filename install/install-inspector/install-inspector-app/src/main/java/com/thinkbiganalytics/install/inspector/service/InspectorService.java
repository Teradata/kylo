package com.thinkbiganalytics.install.inspector.service;

/*-
 * #%L
 * kylo-install-inspector-app
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
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class InspectorService {

    @Value("${info.project.version}")
    private String projectVersion;

    @Value("${inspections.path:}")
    private String inspectionsPath;

    private ExecutorService execService;

    public InspectorService() {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
        execService = new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, queue);
    }

    class AppRunner implements Callable<Configuration> {

        private final String installPath;
        private final String inspectionsPath;
        private final String isDevMode;
        private final String projectVersion;

        public AppRunner(String installPath, String inspectionsPath, String isDevMode, String projectVersion) {

            this.installPath = installPath;
            this.inspectionsPath = inspectionsPath;
            this.isDevMode = isDevMode;
            this.projectVersion = projectVersion;
        }

        @Override
        public Configuration call() throws Exception {
            Configuration config = new DefaultConfiguration(installPath, inspectionsPath, isDevMode, projectVersion);

            ClassLoader previousClassloader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(config.getServicesClassloader());

            try {
                Class<?> inspections = config.getServicesClassloader().loadClass("com.thinkbiganalytics.install.inspector.inspection.Inspections");
                Method inspectMethod = ReflectionUtils.findMethod(inspections, "inspect", String.class, String.class, String.class, String.class);
                Object resultJson = ReflectionUtils.invokeMethod(inspectMethod, inspections.newInstance(), config.getPath(), inspectionsPath, config.isDevMode(), projectVersion);
                ObjectMapper mapper = new ObjectMapper();
                List<Inspection> result = mapper.readValue(resultJson.toString(), new TypeReference<List<InspectionBase>>() {});
                config.setInspections(result);
                return config;
            } finally {
                Thread.currentThread().setContextClassLoader(previousClassloader);
            }
        }
    }

    /**
     * This method runs inspections in separate from main application thread.
     * Running inspections in a separate thread is a workaround for a problem where Spring's DataSourceBuilder doesn't
     * take custom classloader set on Spring's Application Context and instead it takes it from current thread.
     * Without this workaround DataSourceBuilder would end up with Inspector App's classloader which doesn't
     * (and is not supposed to) have classes for running inspections.
     */
    public Configuration inspect(Path installPath) {
        AppRunner runner = new AppRunner(installPath.getUri(), inspectionsPath, installPath.isDevMode().toString(), projectVersion);
        Future<Configuration> futureResult = execService.submit(runner);
        try {
            return futureResult.get(2, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("An error occurred", e);
        } catch (TimeoutException e) {
            throw new IllegalStateException("Operation timed out", e);
        }
    }
}
