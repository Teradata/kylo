package com.thinkbiganalytics.install.inspector.service;

/*-
 * #%L
 * kylo-install-inspector
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
 * %%
 * %% Licensed under the Apache License, Version 2.0 (the "License");
 * %% you may not use this file except in compliance with the License.
 * %% You may obtain a copy of the License at
 * %%
 * %%     http://www.apache.org/licenses/LICENSE-2.0
 * %%
 * %% Unless required by applicable law or agreed to in writing, software
 * %% distributed under the License is distributed on an "AS IS" BASIS,
 * %% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * %% See the License for the specific language governing permissions and
 * %% limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.install.inspector.inspection.Inspection;
import com.thinkbiganalytics.install.inspector.inspection.InspectionStatus;
import com.thinkbiganalytics.install.inspector.inspection.Configuration;
import com.thinkbiganalytics.install.inspector.inspection.Path;
import com.thinkbiganalytics.install.inspector.repository.InspectionRepository;
import com.thinkbiganalytics.install.inspector.repository.ConfigurationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing inspections.
 */
@Service
public class InspectionService {

    private final InspectionRepository inspectionRepo;

    private final ConfigurationRepository configRepo;

    @Autowired
    public InspectionService(InspectionRepository inspectionRepo, ConfigurationRepository configRepo) {
        this.inspectionRepo = inspectionRepo;
        this.configRepo = configRepo;
    }

    public Page<Inspection> getAllInspections() {
        return new PageImpl<>(inspectionRepo.getAll());
    }

    public InspectionStatus execute(int configId, int inspectionId) {
        return configRepo.get(configId).execute(inspectionRepo.get(inspectionId));
    }

    public Configuration setPath(Path path) {
        return configRepo.createConfiguration(path);
    }
}
