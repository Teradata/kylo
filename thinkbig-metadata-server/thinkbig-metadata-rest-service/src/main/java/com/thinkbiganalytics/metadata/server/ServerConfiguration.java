/**
 * 
 */
package com.thinkbiganalytics.metadata.server;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.thinkbiganalytics.metadata.event.EventsContiguration;
import com.thinkbiganalytics.metadata.rest.RestConfiguration;

/**
 *
 * @author Sean Felten
 */
@Configuration
@EnableAutoConfiguration
@Import({ RestConfiguration.class, EventsContiguration.class })
public class ServerConfiguration {


}
