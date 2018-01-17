package com.thinkbiganalytics.install.inspector.inspection;

public class Configuration {

    private Path path;
    private Integer id;

    public Configuration(int id, Path path) {
        this.path = path;
        this.id = id;
    }

    public Path getPath() {
        return path;
    }

    public Integer getId() {
        return id;
    }
}
