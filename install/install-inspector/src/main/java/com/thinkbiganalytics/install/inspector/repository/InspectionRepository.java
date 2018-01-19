package com.thinkbiganalytics.install.inspector.repository;

/*-
 * #%L
 * kylo-install-inspector
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



import com.thinkbiganalytics.install.inspector.inspection.Inspection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

@Repository
public class InspectionRepository {

    private final List<Inspection> inspections;

    private Map<Integer, Inspection> idToCheck = new HashMap<>();

    private int id = 0;

    @Autowired
    public InspectionRepository(List<Inspection> inspections) {
        this.inspections = inspections;
    }

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
