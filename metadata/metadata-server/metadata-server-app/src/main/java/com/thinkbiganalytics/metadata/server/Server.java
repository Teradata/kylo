/**
 * 
 */
package com.thinkbiganalytics.metadata.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 *
 * @author Sean Felten
 */
@SpringBootApplication
//@Import({ ServerConfiguration.class })
public class Server {

    /**
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
        
        
    }

}
