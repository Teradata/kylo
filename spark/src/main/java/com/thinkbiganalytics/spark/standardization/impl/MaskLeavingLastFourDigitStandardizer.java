/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark.standardization.impl;

import com.thinkbiganalytics.spark.standardization.StandardizationPolicy;

/**
 * Masks all digits except the last four
 */
public class MaskLeavingLastFourDigitStandardizer extends SimpleRegexReplacer implements StandardizationPolicy {

     private static final MaskLeavingLastFourDigitStandardizer instance = new MaskLeavingLastFourDigitStandardizer();

    public MaskLeavingLastFourDigitStandardizer() {
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
