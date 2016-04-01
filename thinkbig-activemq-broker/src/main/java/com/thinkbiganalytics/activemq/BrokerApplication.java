/**
 * 
 */
package com.thinkbiganalytics.activemq;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 *
 * @author Sean Felten
 */
//@SpringBootApplication
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
public class BrokerApplication {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerApplication.class);

    @Inject
    private BrokerService broker;
    
    @PostConstruct
    public void startActiveMq() throws  Exception{
        LOG.info("STARTING ActiveMQ Broker " + broker.getBrokerName());
        broker.start();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext cxt = SpringApplication.run(BrokerApplication.class, args);
        BrokerService service = cxt.getBean(BrokerService.class);
        
        service.waitUntilStarted();
        LOG.info("STARTED ActiveMQ Broker at " + service.getTransportConnectors());
        service.waitUntilStopped();
    }

}
