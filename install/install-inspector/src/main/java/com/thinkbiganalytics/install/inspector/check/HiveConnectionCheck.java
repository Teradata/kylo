package com.thinkbiganalytics.install.inspector.check;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HiveConnectionCheck extends DisabledConfigCheck {

    private final Logger log = LoggerFactory.getLogger(HiveConnectionCheck.class);

    @Override
    public String getName() {
        return "Hive Connection Check";
    }

    @Override
    public String getDescription() {
        return "Checks whether Hive connection is setup";
    }

}
