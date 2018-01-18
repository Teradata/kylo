package com.thinkbiganalytics.install.inspector.inspection;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ActiveMqConnectionInspection extends AbstractInspection {

    private final Logger log = LoggerFactory.getLogger(ActiveMqConnectionInspection.class);

    @Override
    public String getName() {
        return "ActiveMQ Connection Check";
    }

    @Override
    public String getDescription() {
        return "Checks whether Kylo Services can connect to ActiveMQ";
    }

}
