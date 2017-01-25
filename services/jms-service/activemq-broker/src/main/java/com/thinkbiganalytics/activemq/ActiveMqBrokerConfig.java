package com.thinkbiganalytics.activemq;

import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.annotation.EnableJms;


/**
 * Created by sr186054 on 3/2/16.
 */
@Configuration
@EnableJms
@PropertySource(value="classpath:activemq.properties", ignoreResourceNotFound=true)
public class ActiveMqBrokerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveMqBrokerConfig.class);

    @Value("${jms.activemq.broker.url:tcp://localhost:61616}")
    private String activeMqBrokerUrl;

    @Bean
    @ConfigurationProperties(prefix="jms.activemq.broker")
    public BrokerService brokerService() throws Exception {
        BrokerService broker = new BrokerService();
        // configure the broker
        broker.setBrokerName("thinkbig-amq-broker");
        broker.setPersistent(false);
        broker.setUseJmx(false);
        broker.addConnector(activeMqBrokerUrl);
        LOG.info("CREATED ActiveMQ Broker at " + broker.getTransportConnectors());
        return broker;
    }

}
