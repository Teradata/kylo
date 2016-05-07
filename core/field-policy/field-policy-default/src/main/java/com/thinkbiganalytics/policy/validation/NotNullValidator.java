package com.thinkbiganalytics.policy.validation;

import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;
import com.thinkbiganalytics.policy.PropertyLabelValue;

import org.apache.commons.lang3.StringUtils;

@Validator(name = "Not Null", description = "Validate a value is not null")
public class NotNullValidator implements ValidationPolicy {


    @PolicyProperty(name = "EMPTY_STRING", value = "false", displayName = "Allow Empty String Values",
                    hint = "If the value is a String, are empty strings not null?", type = PolicyProperty.PROPERTY_TYPE.select,
                    labelValues = {@PropertyLabelValue(label = "Yes", value = "true"),
                                   @PropertyLabelValue(label = "No", value = "false")})
    private boolean allowEmptyString = false;

    @PolicyProperty(name = "TRIM_STRING", value = "true", displayName = "Trim String Values",
                    hint = " If the value is a String, should it be trimmed before checking for null?",
                    type = PolicyProperty.PROPERTY_TYPE.select, labelValues = {@PropertyLabelValue(label = "Yes", value = "true"),
                                                                               @PropertyLabelValue(label = "No",
                                                                                                   value = "false")})
    private boolean trimString = true;

    public NotNullValidator(@PolicyPropertyRef(name = "EMPTY_STRING") boolean allowEmptyString,
                            @PolicyPropertyRef(name = "TRIM_STRING") boolean trimString) {
        this.allowEmptyString = allowEmptyString;
        this.trimString = trimString;
    }

    @Override
    public boolean validate(Object value) {

        if (value == null) {
            return false;
        }
        if (!allowEmptyString) {
            String svalue = (trimString ? StringUtils.trim((String) value) : (String) value);
            return (!StringUtils.isEmpty(svalue));
        }
        return true;
    }


    public boolean isAllowEmptyString() {
        return allowEmptyString;
    }

    public boolean isTrimString() {
        return trimString;
    }
}
