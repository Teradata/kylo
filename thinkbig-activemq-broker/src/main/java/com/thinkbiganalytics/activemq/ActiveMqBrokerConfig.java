/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.activemq;

import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

import javax.annotation.PostConstruct;


/**
 * Created by sr186054 on 3/2/16.
 */
@Configuration
@EnableJms
public class ActiveMqBrokerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveMqBrokerConfig.class);

    @Value("${spring.activemq.broker-url}")
    private String activeMqBrokerUrl;


    @PostConstruct
    public void startActiveMq() throws  Exception{
        BrokerService broker = new BrokerService();
        // configure the broker
        broker.setBrokerName("thinkbig-amq-broker");
        broker.setPersistent(false);
        broker.setUseJmx(false);
        broker.addConnector(activeMqBrokerUrl);
        broker.start();
        LOG.info("STARTED ActiveMQ Broker at "+activeMqBrokerUrl);
    }

}
