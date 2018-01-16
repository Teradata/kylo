package com.thinkbiganalytics.install.inspector.check;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ActiveMqConnectionCheck extends DisabledConfigCheck {

    private final Logger log = LoggerFactory.getLogger(ActiveMqConnectionCheck.class);

    @Override
    public String getName() {
        return "ActiveMQ Connection Check";
    }

    @Override
    public String getDescription() {
        return "Checks whether Kylo Services can connect to ActiveMQ";
    }

}
