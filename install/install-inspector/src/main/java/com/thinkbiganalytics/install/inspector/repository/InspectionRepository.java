package com.thinkbiganalytics.install.inspector.repository;


import com.thinkbiganalytics.install.inspector.inspection.Inspection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

@Repository
public class InspectionRepository {

    @Autowired
    private List<Inspection> inspections;

    private Map<Integer, Inspection> idToCheck = new HashMap<>();

    private int id = 0;

    @PostConstruct
    public void postConstruct() {
        inspections.forEach(check -> {
            check.setId(nextId());
            idToCheck.put(check.getId(), check);
        });
    }

    private int nextId() {
        return id++;
    }

    public List<Inspection> getAll() {
        return inspections;
    }

    public Inspection get(int id) {
        return idToCheck.get(id);
    }
}
