/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.standardization;


/**
 * Masks all digits except the last four
 */
@Standardizer(name = "Mask Credit Card", description = "Preserves last 4 digits")
public class MaskLeavingLastFourDigitStandardizer extends SimpleRegexReplacer implements StandardizationPolicy {

    private static final MaskLeavingLastFourDigitStandardizer instance = new MaskLeavingLastFourDigitStandardizer();

    private MaskLeavingLastFourDigitStandardizer() {
        super("\\d", "X");
    }

    @Override
    public String convertValue(String value) {
        if (value.length() > 4) {
            return super.convertValue(value.substring(0, value.length() - 4)) + value.substring(value.length() - 4);
        }
        return value;
    }

    public static MaskLeavingLastFourDigitStandardizer instance() {
        return instance;
    }

}
