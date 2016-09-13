package com.thinkbiganalytics.spark;

import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.spark.repl.ScriptEngine;
import com.thinkbiganalytics.spark.repl.ScriptEngineFactory;
import com.thinkbiganalytics.spark.rest.SparkShellTransformController;
import com.thinkbiganalytics.spark.service.TransformService;

import org.apache.spark.SparkConf;
import org.apache.spark.util.ShutdownHookManager;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.WebSocketAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import scala.Function0;
import scala.runtime.AbstractFunction0;
import scala.runtime.BoxedUnit;

/**
 * Instantiates a REST server for executing Spark scripts.
 */
@PropertySource(value = {"classpath:applicationDefaults.properties", "classpath:application.properties", "classpath:applicationDevOverride.properties"}, ignoreResourceNotFound = true)
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
    public EmbeddedServletContainerFactory getEmbeddedServletContainer () {
        return new TomcatEmbeddedServletContainerFactory();
    }

    /**
     * Gets the resource configuration for setting up Jersey.
     *
     * @param sparkPort the port number for Spark UI
     * @return the Jersey configuration
     */
    @Bean
    public ResourceConfig getJerseyConfig (@Value("${spark.ui.port:8451}") String sparkPort,
                                           @Value("${kerberos.thinkbig.kerberosEnabled:false}") String kerberosEnabled,
                                           @Value("${kerberos.thinkbig.hadoopConfigurationResources}") String hadoopConfigurationResources,
                                           @Value("${kerberos.thinkbig.kerberosPrincipal}") String kerberosPrincipal,
                                           @Value("${kerberos.thinkbig.keytabLocation}") String keytabLocation) {
        ResourceConfig config = new ResourceConfig(ApiListingResource.class, SwaggerSerializers.class, SparkShellTransformController.class);

        SparkConf conf = new SparkConf().setAppName("SparkShellServer").set("spark.ui.port", sparkPort);
        final ScriptEngine scriptEngine = ScriptEngineFactory.getScriptEngine(conf);
        final TransformService transformService = createTransformService(scriptEngine, createKerberosTicketConfiguration(kerberosEnabled, hadoopConfigurationResources, kerberosPrincipal, keytabLocation));
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
                        return transformService;
                    }
                }).to(TransformService.class).in(RequestScoped.class);
            }
        });

        return config;
    }

    private KerberosTicketConfiguration createKerberosTicketConfiguration(String kerberosEnabled, String hadoopConfigurationResources, String kerberosPrincipal, String keytabLocation ) {
        KerberosTicketConfiguration config = new KerberosTicketConfiguration();
        config.setKerberosEnabled("true".equals(kerberosEnabled) ? true: false);
        config.setHadoopConfigurationResources(hadoopConfigurationResources);
        config.setKerberosPrincipal(kerberosPrincipal);
        config.setKeytabLocation(keytabLocation);

        return config;
    }

    /**
     * Create a transform service using the specified script engine.
     *
     * @param engine the script engine
     * @return the transform service
     */
    private static TransformService createTransformService(@Nonnull final ScriptEngine engine, KerberosTicketConfiguration kerberosThinkbigConfiguration) {
        // Start the service
        final TransformService service = new TransformService(engine, kerberosThinkbigConfiguration);
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
