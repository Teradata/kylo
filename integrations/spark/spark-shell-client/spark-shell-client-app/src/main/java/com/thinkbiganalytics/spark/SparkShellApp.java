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

import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.spark.rest.SparkShellTransformController;
import com.thinkbiganalytics.spark.service.TransformService;

import org.apache.spark.SparkConf;
import org.apache.spark.util.ShutdownHookManager;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.WebSocketAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.Nonnull;

import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import scala.Function0;
import scala.runtime.AbstractFunction0;
import scala.runtime.BoxedUnit;

/**
 * Instantiates a REST server for executing Spark scripts.
 */
@PropertySource(value = {"classpath:sparkDefaults.properties", "classpath:spark.properties", "classpath:sparkDevOverride.properties"}, ignoreResourceNotFound = true)
@SpringBootApplication(exclude = {VelocityAutoConfiguration.class, WebSocketAutoConfiguration.class})  // ignore auto-configuration classes outside Spark Shell
public class SparkShellApp {

    @Autowired
    private TransformService service;

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
     * Creates the Spark configuration.
     *
     * @param sparkPort the Spark UI port
     * @return the Spark configuration
     */
    @Bean
    public SparkConf sparkConf(@Value("${spark.ui.port:8451}") String sparkPort) {
        return new SparkConf().setAppName("SparkShellServer").set("spark.ui.port", sparkPort);
    }

    /**
     * Gets the resource configuration for setting up Jersey.
     *
     * @return the Jersey configuration
     */
    @Bean
    public ResourceConfig getJerseyConfig(@Value("${kerberos.spark.kerberosEnabled:false}") String kerberosEnabled,
                                          @Value("${kerberos.spark.hadoopConfigurationResources}") String hadoopConfigurationResources,
                                          @Value("${kerberos.spark.kerberosPrincipal}") String kerberosPrincipal,
                                          @Value("${kerberos.spark.keytabLocation}") String keytabLocation) {
        ResourceConfig config = new ResourceConfig(ApiListingResource.class, SwaggerSerializers.class, SparkShellTransformController.class);
        startTransformationService(createKerberosTicketConfiguration(kerberosEnabled, hadoopConfigurationResources, kerberosPrincipal, keytabLocation));
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
     * Gets the Kerberos configuration.
     *
     * @param kerberosEnabled              {@code true} if Kerberos authentication is enabled, or {@code false} otherwise
     * @param hadoopConfigurationResources the paths to the Hadoop configuration files
     * @param kerberosPrincipal            the Kerberos principal for authentication
     * @param keytabLocation               the path to the keytab file
     * @return the Kerberos configuration
     */
    private KerberosTicketConfiguration createKerberosTicketConfiguration(@Nonnull final String kerberosEnabled, @Nonnull final String hadoopConfigurationResources,
                                                                          @Nonnull final String kerberosPrincipal, @Nonnull final String keytabLocation) {
        KerberosTicketConfiguration config = new KerberosTicketConfiguration();
        config.setKerberosEnabled("true".equals(kerberosEnabled));
        config.setHadoopConfigurationResources(hadoopConfigurationResources);
        config.setKerberosPrincipal(kerberosPrincipal);
        config.setKeytabLocation(keytabLocation);
        return config;
    }

    /**
     * Create a transform service using the specified script engine.
     *
     * @return the transform service
     */
    private TransformService startTransformationService(KerberosTicketConfiguration kerberosThinkbigConfiguration) {
        // Start the service
        service.setKerberosTicketConfiguration(kerberosThinkbigConfiguration);
        service.startAsync();

        // Add a shutdown hook
        Function0<BoxedUnit> hook = new AbstractFunction0<BoxedUnit>() {
            @Override
            public BoxedUnit apply() {
                service.stopAsync();
                service.awaitTerminated();
                return BoxedUnit.UNIT;
            }
        };
        ShutdownHookManager.addShutdownHook(hook);

        // Wait for service to start
        service.awaitRunning();
        return service;
    }
}
