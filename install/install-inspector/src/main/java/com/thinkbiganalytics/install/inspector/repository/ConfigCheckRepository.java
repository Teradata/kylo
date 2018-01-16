package com.thinkbiganalytics.install.inspector.repository;


import com.thinkbiganalytics.install.inspector.check.ConfigCheck;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

@Repository
public class ConfigCheckRepository {

    @Autowired
    private List<ConfigCheck> configChecks;

    private Map<Integer, ConfigCheck> idToCheck = new HashMap<>();

    private int id = 0;

    @PostConstruct
    public void postConstruct() {
        configChecks.forEach(check -> {
            check.setId(nextId());
            idToCheck.put(check.getId(), check);
        });
    }

    private int nextId() {
        return id++;
    }

    public List<ConfigCheck> getAll() {
        return configChecks;
    }

    public ConfigCheck get(int id) {
        return idToCheck.get(id);
    }
}
