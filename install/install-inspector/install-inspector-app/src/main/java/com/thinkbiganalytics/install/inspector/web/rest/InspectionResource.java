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
import com.thinkbiganalytics.install.inspector.inspection.Path;
import com.thinkbiganalytics.install.inspector.service.InspectorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class InspectionResource {

    private final Logger log = LoggerFactory.getLogger(InspectionResource.class);

    @Inject
    private InspectorService inspectorService;

    /**
     * PUT  /config  : Creates or updates Kylo Configuration at given path
     * <p>
     *
     * @param installPath installation path
     */
    @RequestMapping(method = RequestMethod.POST, path="/configuration")
    public ResponseEntity<Configuration> createConfiguration(@Valid @RequestBody Path installPath) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        log.debug("REST request to set new Kylo installation path : {}", installPath);
        Configuration config = inspectorService.inspect(installPath);
        return new ResponseEntity<>(config, HttpStatus.OK);
    }
}
