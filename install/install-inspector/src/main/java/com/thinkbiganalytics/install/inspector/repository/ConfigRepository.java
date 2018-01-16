package com.thinkbiganalytics.install.inspector.repository;

import com.thinkbiganalytics.install.inspector.check.Configuration;
import com.thinkbiganalytics.install.inspector.check.Path;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class ConfigRepository {

    private Map<Integer, Configuration> idToConfiguration = new HashMap<>();

    private int id = 0;

    private int nextId() {
        return id;
    }

    public Configuration get(int id) {
        return idToConfiguration.get(id);
    }

    public Configuration createConfiguration(Path path) {
        //use Spring to load configuration and resolve properties
        Configuration c = new Configuration(nextId(), path);
        idToConfiguration.put(c.getId(), c);
        return c;
    }
}
