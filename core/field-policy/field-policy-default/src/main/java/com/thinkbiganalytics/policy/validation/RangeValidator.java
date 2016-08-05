/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.validation;


import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;

import org.apache.commons.lang3.Validate;

@Validator(name = "Range", description = "Numeric must fall within range")
public class RangeValidator implements ValidationPolicy<Number> {

    @PolicyProperty(name = "Min", type = PolicyPropertyTypes.PROPERTY_TYPE.number, hint = "Minimum Value")
    private Double min;
    @PolicyProperty(name = "Max", type = PolicyPropertyTypes.PROPERTY_TYPE.number, hint = "Maximum Value")
    private Double max;

    public RangeValidator(@PolicyPropertyRef(name = "Min") Number min, @PolicyPropertyRef(name = "Max") Number max) {
        super();
        this.min = (min != null ? min.doubleValue() : null);
        this.max = (max != null ? max.doubleValue() : null);
        if (min != null && max != null) {
            Validate.isTrue(this.min <= this.max, "Minimum must smaller than Maximum");
        }
    }

    @Override
    public boolean validate(Number value) {
        if (value == null) {
            return true;
        }
        double dval = value.doubleValue();
        if (min != null) {
            if (dval < min) {
                return false;
            }
        }
        if (max != null) {
            if (dval > max) {
                return false;
            }
        }
        return true;
    }

    public Number getMin() {
        return min;
    }

    public Number getMax() {
        return max;
    }
}
