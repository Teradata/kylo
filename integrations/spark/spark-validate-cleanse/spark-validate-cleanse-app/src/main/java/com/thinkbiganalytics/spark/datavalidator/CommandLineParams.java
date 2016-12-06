package com.thinkbiganalytics.spark.datavalidator;

import com.beust.jcommander.Parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines acceptable command line parameters for Validator application.
 */
public class CommandLineParams implements Serializable {

    @Parameter(names = {"-h", "--hiveConf"}, description = "Hive configuration parameters", converter = ParameterConverter.class)
    private List<Param> hiveParams;

    public List<Param> getHiveParams() {
        return hiveParams == null ? new ArrayList<Param>(0) : hiveParams;
    }
}
