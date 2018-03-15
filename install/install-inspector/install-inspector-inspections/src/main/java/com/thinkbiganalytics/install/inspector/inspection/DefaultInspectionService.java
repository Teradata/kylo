package com.thinkbiganalytics.install.inspector.inspection;

/*-
 * #%L
 * kylo-service-app
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultInspectionService implements InspectionService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultInspectionService.class);

    private final List<Inspection> inspections;

    @Autowired
    public DefaultInspectionService(List<Inspection> inspections) {
        this.inspections = inspections;
    }

    private List<Inspection> getInspections() {
        return inspections;
    }

    @Override
    public Object inspect(Configuration configuration) {

        List<Inspection> inspections = getInspections();
        for (Inspection inspection : inspections) {
            InspectionStatus status;
            try {
                status = inspection.inspect(configuration);
            } catch (Exception e) {
                status = new InspectionStatus(false);
                String exceptionMsg = e.getMessage();
                String msg = String.format("An error occurred running inspection '%s': %s", inspection.getName(), exceptionMsg == null ? "No details available" : exceptionMsg);
                status.addError(msg);
                LOG.error(msg, e);
            }
            inspection.setStatus(status);
        }

        try {
            return new ObjectMapper().writeValueAsString(inspections);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("An error occurred while serialising inspections", e);
        }
    }


}
