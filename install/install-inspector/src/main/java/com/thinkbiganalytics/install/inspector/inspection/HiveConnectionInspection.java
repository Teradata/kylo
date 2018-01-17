package com.thinkbiganalytics.install.inspector.inspection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HiveConnectionInspection extends DisabledInspection {

    private final Logger log = LoggerFactory.getLogger(HiveConnectionInspection.class);

    @Override
    public String getName() {
        return "Hive Connection Check";
    }

    @Override
    public String getDescription() {
        return "Checks whether Hive connection is setup";
    }

}
