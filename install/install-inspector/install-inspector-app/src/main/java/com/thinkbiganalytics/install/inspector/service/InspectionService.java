package com.thinkbiganalytics.install.inspector.service;

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


import com.thinkbiganalytics.install.inspector.repository.ConfigurationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InspectionService {

    private final ConfigurationRepository configRepo;

    @Autowired
    public InspectionService(ConfigurationRepository configRepo) {
        this.configRepo = configRepo;
    }

//    public InspectionStatus execute(int configId, int inspectionId) {
//        Inspection inspection = inspectionRepo.get(inspectionId);
//        try {
//            return inspection.inspect(configRepo.get(configId));
//        } catch (Exception e) {
//            String msg = String.format("An error occurred while running configuration inspection '%s'", inspection.getName());
//            InspectionStatus status = new InspectionStatus(false);
//            status.addError(msg + ": " + e.getMessage());
//            return status;
//        }
//    }
//
//    public Configuration createConfiguration(Path path) {
//        return configRepo.create(path);
//    }
}
