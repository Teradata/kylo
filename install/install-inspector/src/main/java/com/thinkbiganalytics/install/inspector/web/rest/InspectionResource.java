package com.thinkbiganalytics.install.inspector.web.rest;

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


import com.thinkbiganalytics.install.inspector.inspection.Configuration;
import com.thinkbiganalytics.install.inspector.inspection.Inspection;
import com.thinkbiganalytics.install.inspector.inspection.InspectionStatus;
import com.thinkbiganalytics.install.inspector.inspection.Path;
import com.thinkbiganalytics.install.inspector.service.InspectionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.List;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class InspectionResource {

    private final Logger log = LoggerFactory.getLogger(InspectionResource.class);

    private final InspectionService inspectionService;

    @Autowired
    public InspectionResource(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    /**
     * GET /inspection : get all configuration inspections
     *
     * @return the ResponseEntity with status 200 (OK) and with body having all configuration inspections
     */
    @GetMapping("/inspection")
    public ResponseEntity<List<Inspection>> getAllConfigChecks() {
        final List<Inspection> page = inspectionService.getAllInspections();
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    /**
     * PUT  /config  : Creates or updates Kylo Configuration at given path
     * <p>
     *
     * @param installPath installation path
     */
    @PostMapping("/configuration")
    public ResponseEntity<Configuration> createConfiguration(@Valid @RequestBody Path installPath) throws URISyntaxException {
        log.debug("REST request to set new Kylo installation path : {}", installPath);
        Configuration config = inspectionService.createConfiguration(installPath);
        return new ResponseEntity<>(config, HttpStatus.OK);
    }

    /**
     * POST  /config  : Run configuration check
     * <p>
     *
     * @param configId configuration id
     * @param inspectionId configuration inspection id
     */
    @GetMapping("/configuration/{configId}/{inspectionId}")
    public ResponseEntity<InspectionStatus> runConfigCheck(@Valid @PathVariable("configId") Integer configId, @Valid @PathVariable("inspectionId") Integer inspectionId) throws URISyntaxException {
        log.debug("REST request to execute configuration inspection : {}", inspectionId);
        InspectionStatus status = inspectionService.execute(configId, inspectionId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

}
