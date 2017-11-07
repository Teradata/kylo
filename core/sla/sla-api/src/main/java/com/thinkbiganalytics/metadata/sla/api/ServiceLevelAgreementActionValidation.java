package com.thinkbiganalytics.metadata.sla.api;

/*-
 * #%L
 * thinkbig-sla-api
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

/**
 */
public class ServiceLevelAgreementActionValidation {

    public static final ServiceLevelAgreementActionValidation VALID = new ServiceLevelAgreementActionValidation(true);
    public static final ServiceLevelAgreementActionValidation INVALID = new ServiceLevelAgreementActionValidation(false);
    private String validationMessage;
    private String actionClass;
    private boolean valid;

    public ServiceLevelAgreementActionValidation(boolean valid) {
        this.valid = valid;
    }

    public ServiceLevelAgreementActionValidation(boolean valid, String validationMessage) {
        this.valid = valid;
        this.validationMessage = validationMessage;
    }

    public static ServiceLevelAgreementActionValidation valid(Class actionClass) {
        ServiceLevelAgreementActionValidation validation = new ServiceLevelAgreementActionValidation(true);
        validation.setActionClass(actionClass.getName());
        return validation;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public String getActionClass() {
        return actionClass;
    }

    public void setActionClass(String actionClass) {
        this.actionClass = actionClass;
    }
}
