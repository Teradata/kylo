/**
 * 
 */
package com.thinkbiganalytics.metadata.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 *
 * @author Sean Felten
 */
@Configuration
@SpringBootApplication
@EnableAutoConfiguration(exclude = {VelocityAutoConfiguration.class})
@EnableConfigurationProperties
@Import({ ServerConfiguration.class })
@ComponentScan("com.thinkbiganalytics") //FIX this
public class Server {

    /**
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
        
        
    }

}
