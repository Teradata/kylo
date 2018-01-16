package com.thinkbiganalytics.install.inspector.check;

public abstract class AbstractConfigCheck implements ConfigCheck {

    private final ConfigStatus status;
    private int id;

    public AbstractConfigCheck() {
        this(ConfigStatus.INITIAL);
    }

    public AbstractConfigCheck(ConfigStatus status) {
        this.status = status;
    }

    @Override
    public ConfigStatus getStatus() {
        return status;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public ConfigStatus execute(Configuration configuration) {
        throw new IllegalStateException("Not implemented yet");
    }
}
