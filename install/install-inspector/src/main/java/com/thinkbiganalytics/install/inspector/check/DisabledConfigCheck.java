package com.thinkbiganalytics.install.inspector.check;

public abstract class DisabledConfigCheck extends AbstractConfigCheck {

    public DisabledConfigCheck() {
        super(ConfigStatus.DISABLED);
    }

}
