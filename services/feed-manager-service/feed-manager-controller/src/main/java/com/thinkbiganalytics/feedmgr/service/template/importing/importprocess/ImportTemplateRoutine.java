package com.thinkbiganalytics.feedmgr.service.template.importing.importprocess;
/*-
 * #%L
 * thinkbig-feed-manager-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
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
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgressMessage;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.NiFiTemplateImport;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;

/**
 * Created by sr186054 on 12/11/17.
 */
public interface ImportTemplateRoutine {

    /**
     * Creates a new instance of the template as a flow in NiFi
     */
    NifiProcessGroup create(NiFiTemplateImport niFiTemplateImport, UploadProgressMessage importStatusMessage);

    /**
     * Imports a template into NiFi.
     *
     * This will not create a instance of the template. This just adds it to the NiFi template list
     */
    NiFiTemplateImport importIntoNiFi(ImportTemplate template, ImportTemplateOptions importOptions);

    /**
     * Import, create, validate, connect this template
     */
    boolean importTemplate();

    /**
     * This will import into NiFi and create the Instance in NiFi.
     * This will NOT validate/connect the instance after it has been created in NiFi.
     */
    boolean importIntoNiFiAndCreateInstance();

    /**
     * Connect output ports to other inputs
     */
    boolean connectAndValidate();

    /**
     * After the template has been created, this will validate to ensure its ok
     */
    boolean validateInstance();


    /**
     * Rollback any changes to its original state
     */
    boolean rollback();

    /**
     * Remove any temporary flows/data created during import
     */
    void cleanup();


}
