package com.thinkbiganalytics.spark.datavalidator;

/*-
 * #%L
 * thinkbig-spark-validate-cleanse-app
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

import com.thinkbiganalytics.policy.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Store the result of all standardizations and validations on a field
 */
public class StandardizationAndValidationResult {

    private List<ValidationResult> validationResults = null;

    private Object fieldValue = null;

    public StandardizationAndValidationResult(Object value){
        fieldValue = value;
    }


    public ValidationResult getFinalValidationResult(){
        ValidationResult finalResult = StandardDataValidator.VALID_RESULT;
        if(validationResults == null || validationResults.isEmpty()){
            finalResult = StandardDataValidator.VALID_RESULT;
        }
        else {
            for(ValidationResult r : validationResults) {
                if(r != StandardDataValidator.VALID_RESULT){
                    finalResult = r;
                    break;
                }
            }
        }
        return finalResult;
    }

    public void addValidationResult(ValidationResult validationResult){
        validationResults = (validationResults == null ? new ArrayList<ValidationResult>() : validationResults);
        validationResults.add(validationResult);
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
    }

    public List<ValidationResult> getValidationResults() {
        return validationResults;
    }

    public String getFieldValueForValidation(){
        return((fieldValue == null) ? null:fieldValue.toString());
    }
}
