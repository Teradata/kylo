package com.thinkbiganalytics.install.inspector.inspection;

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


import java.util.ArrayList;
import java.util.List;

public class InspectionStatus {

    private boolean valid;
    private List<String> descriptions = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public InspectionStatus() {
        //default constructor required for de-serializing response from another classloader
    }

    public InspectionStatus(boolean isValid) {
        this.valid = isValid;
    }

    public boolean isValid() {
        return valid;
    }

    public void addDescription(String description) {
        this.descriptions.add(description);
    }

    public List<String> getDescriptions() {
        return descriptions;
    }

    public void addError(String error) {
        this.valid = false;
        this.errors.add(error);
    }

    public List<String> getErrors() {
        return errors;
    }

    public InspectionStatus and(InspectionStatus other) {
        InspectionStatus status = new InspectionStatus(this.valid && other.valid);
        status.getErrors().addAll(this.errors);
        status.getErrors().addAll(other.errors);
        status.getDescriptions().addAll(this.descriptions);
        status.getDescriptions().addAll(other.descriptions);
        return status;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}

