package com.thinkbiganalytics.policy.validation;

/*-
 * #%L
 * thinkbig-field-policy-default
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

import java.io.Serializable;

/**
 * Represents the result of a single validation of a field
 */
public class ValidationResult implements Serializable {

    boolean result = true;
    String scope;
    String fieldName;
    String failType;
    String rule;
    String rejectReason;

    public ValidationResult() {
        result = true;
    }

    ValidationResult(String scope, String fieldName, String failType, String rule, String rejectReason) {
        this.result = false;
        this.scope = scope;
        this.fieldName = fieldName;
        this.failType = failType;
        this.rule = rule;
        this.rejectReason = rejectReason;
    }

    public static ValidationResult failRow(String failType, String rejectReason) {
        return new ValidationResult("row", null, failType, null, rejectReason);
    }

    public static ValidationResult failField(String failType, String fieldName, String rejectReason) {
        return new ValidationResult("field", fieldName, failType, null, rejectReason);
    }

    public static ValidationResult failFieldRule(String failType, String fieldName, String rule, String rejectReason) {
        return new ValidationResult("field", fieldName, failType, rule, rejectReason);
    }

    public String toJSON() {
        StringBuffer sb = new StringBuffer();
        sb.append("{\"scope\":\"").append(scope).append("\",");
        if (fieldName != null) {
            sb.append("\"field\":\"").append(fieldName).append("\",");
        }
        sb.append("\"type\":\"").append(failType).append("\",");
        if (rule != null) {
            sb.append("\"rule\":\"").append(rule).append("\",");
        }
        sb.append("\"reason\":\"").append(rejectReason).append("\"}");
        return sb.toString();
    }

    public boolean isValid() {
        return result;
    }

}
