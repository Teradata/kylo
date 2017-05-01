package com.thinkbiganalytics.spark;

/*-
 * #%L
 * thinkbig-spark-shell-client-app
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.thinkbiganalytics.spark.dataprofiler.Profiler;
import com.thinkbiganalytics.spark.metadata.TransformScript;
import com.thinkbiganalytics.spark.repl.SparkScriptEngine;
import com.thinkbiganalytics.spark.rest.SparkShellTransformController;
import com.thinkbiganalytics.spark.service.TransformJobTracker;
import com.thinkbiganalytics.spark.service.TransformService;
import com.thinkbiganalytics.spark.shell.DatasourceProviderFactory;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SQLContext;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.WebSocketAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.ResourcePropertySource;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Nullable;

import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

/**
 * Instantiates a REST server for executing Spark scripts.
 */
@ComponentScan("com.thinkbiganalytics.spark")
@PropertySource(value = {"classpath:sparkDefaults.properties", "classpath:spark.properties", "classpath:sparkDevOverride.properties"}, ignoreResourceNotFound = true)
@SpringBootApplication(exclude = {VelocityAutoConfiguration.class, WebSocketAutoConfiguration.class})  // ignore auto-configuration classes outside Spark Shell
public class SparkShellApp {

    /**
     * Instantiates the REST server with the specified arguments.
     *
     * @param args the command-line arguments
     * @throws Exception if an error occurs
     */
    public static void main(String[] args) throws Exception {
        SpringApplication.run(SparkShellApp.class, args);
    }

    /**
     * Gets the factory for the embedded web server.
     *
     * @return the embedded servlet container factory
     */
    @Bean
    public EmbeddedServletContainerFactory getEmbeddedServletContainer() {
        return new TomcatEmbeddedServletContainerFactory();
    }

    /**
     * Gets the resource configuration for setting up Jersey.
     *
     * @return the Jersey configuration
     */
    @Bean
    public ResourceConfig jerseyConfig(final TransformService service) {
        ResourceConfig config = new ResourceConfig(ApiListingResource.class, SwaggerSerializers.class, SparkShellTransformController.class);
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(new Factory<TransformService>() {
                    @Override
                    public void dispose(TransformService instance) {
                        // nothing to do
                    }

                    @Override
                    public TransformService provide() {
                        return service;
                    }
                }).to(TransformService.class).in(RequestScoped.class);
            }
        });

        return config;
    }

    /**
     * Creates the Spark configuration.
     *
     * @return the Spark configuration
     */
    @Bean
    public SparkConf sparkConf(final Environment env) {
        final SparkConf conf = new SparkConf().setAppName("SparkShellServer").set("spark.ui.port", "8451");

        final Iterable<Map.Entry<String, Object>> properties = FluentIterable.from(Collections.singleton(env))
            .filter(AbstractEnvironment.class)
            .transformAndConcat(new Function<AbstractEnvironment, Iterable<?>>() {
                @Nullable
                @Override
                public Iterable<?> apply(@Nullable final AbstractEnvironment input) {
                    return (input != null) ? input.getPropertySources() : null;
                }
            })
            .filter(ResourcePropertySource.class)
            .transform(new Function<ResourcePropertySource, Map<String, Object>>() {
                @Nullable
                @Override
                public Map<String, Object> apply(@Nullable final ResourcePropertySource input) {
                    return (input != null) ? input.getSource() : null;
                }
            })
            .transformAndConcat(new Function<Map<String, Object>, Iterable<Map.Entry<String, Object>>>() {
                @Nullable
                @Override
                public Iterable<Map.Entry<String, Object>> apply(@Nullable final Map<String, Object> input) {
                    return (input != null) ? input.entrySet() : null;
                }
            })
            .filter(new Predicate<Map.Entry<String, Object>>() {
                @Override
                public boolean apply(@Nullable final Map.Entry<String, Object> input) {
                    return (input != null && input.getKey().startsWith("spark."));
                }
            });
        for (final Map.Entry<String, Object> entry : properties) {
            conf.set(entry.getKey(), entry.getValue().toString());
        }

        return conf;
    }

    /**
     * Gets the Spark SQL context.
     *
     * @param engine the Spark script engine
     * @return the Spark SQL context
     */
    @Bean
    public SQLContext sqlContext(final SparkScriptEngine engine) {
        return engine.getSQLContext();
    }

    /**
     * Gets the transform service.
     *
     * @param transformScriptClass      the transform script class
     * @param engine                    the Spark script engine
     * @param sparkContextService       the Spark context service
     * @param tracker                   the transform job tracker
     * @param datasourceProviderFactory the data source provider factory
     * @param profiler                  the profiler
     * @return the transform service
     */
    @Bean
    public TransformService transformService(final Class<? extends TransformScript> transformScriptClass, final SparkScriptEngine engine, final SparkContextService sparkContextService,
                                             final TransformJobTracker tracker, final DatasourceProviderFactory datasourceProviderFactory, final Profiler profiler) {
        final TransformService service = new TransformService(transformScriptClass, engine, sparkContextService, tracker);
        service.setDatasourceProviderFactory(datasourceProviderFactory);
        service.setProfiler(profiler);
        return service;
    }
}
