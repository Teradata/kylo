/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.com.thinkbiganalytics.standardization;


import com.thinkbiganalytics.policies.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policies.standardization.Standardizer;

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
            return super.convertValue(value.substring(0, value.length()-5))+value.substring(value.length()-5);
        }
        return value;
    }

    public static MaskLeavingLastFourDigitStandardizer instance() {
        return instance;
    }


    public static void main(String[] args) {
        MaskLeavingLastFourDigitStandardizer cc = MaskLeavingLastFourDigitStandardizer.instance();
        System.out.println(cc.convertValue("5100145505218790"));
        System.out.println(cc.convertValue("5100-1455-0521-8790"));
        System.out.println(cc.convertValue("560-60-2015"));
        System.out.println(cc.convertValue("2015"));
    }
}
