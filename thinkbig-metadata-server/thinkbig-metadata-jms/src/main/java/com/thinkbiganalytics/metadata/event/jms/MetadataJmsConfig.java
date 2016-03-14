/**
 * 
 */
package com.thinkbiganalytics.metadata.event.jms;

import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Sean Felten
 */
@Configuration
@ComponentScan({ "com.thinkbiganalytics.activemq" })
public class MetadataJmsConfig {

    @Bean(name="datasourceChangeTopic")
    public Topic datasourceChangeTopic(){
        ActiveMQTopic topic = new ActiveMQTopic(MetadataTopics.DATASOURCE_CHANGE);
        return topic;
    }
    
    @Bean(name="preconditionTriggerTopic")
    public Topic preconditionTriggerTopic(){
        ActiveMQTopic topic = new ActiveMQTopic(MetadataTopics.PRECONDITION_TRIGGER);
        return topic;
    }

}
