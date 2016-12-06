package com.thinkbiganalytics.spark.datavalidator;

import com.beust.jcommander.IStringConverter;

/**
 * Converts "name=value" pairs which are found on command line into an instance of Param class.
 */
public class ParameterConverter implements IStringConverter<Param> {
    @Override
    public Param convert(String value) {
        String[] s = value.split("=");
        return new Param(s[0], s[1]);
    }
}
