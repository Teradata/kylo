package com.thinkbiganalytics.feedmgr.service.template.importing.validation;
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
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sr186054 on 12/11/17.
 */
public class ValidateImportTemplateXml extends AbstractValidateImportTemplate {

    private static final Logger log = LoggerFactory.getLogger(ValidateImportTemplateXml.class);


    public ValidateImportTemplateXml(ImportTemplate importTemplate, ImportTemplateOptions importTemplateOptions){
       super(importTemplate,importTemplateOptions);
    }

    public Logger getLogger(){
        return log;
    }



    public boolean validate(){

        //deal with reusable templates??
        validateNiFiTemplateImport();

       return this.importTemplate.isValid();
    }



}
