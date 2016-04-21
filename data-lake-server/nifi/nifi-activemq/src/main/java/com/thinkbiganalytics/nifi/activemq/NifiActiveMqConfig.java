package com.thinkbiganalytics.nifi.activemq;

import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.Topic;

/**
 * Created by sr186054 on 3/3/16.
 */
@Configuration
public class NifiActiveMqConfig {

        @Bean
        @Qualifier(Topics.NIFI_EVENT_TOPIC_BEAN)
        public Topic topic(){
            ActiveMQTopic topic = new ActiveMQTopic(Topics.THINKBIG_NIFI_EVENT_TOPIC);
            return topic;
        }

}
