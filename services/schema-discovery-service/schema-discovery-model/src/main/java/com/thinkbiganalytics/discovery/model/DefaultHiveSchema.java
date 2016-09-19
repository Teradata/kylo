/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.discovery.schema.HiveTableSchema;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultHiveSchema extends DefaultTableSchema implements HiveTableSchema {

    private String hiveFormat;

    public String getHiveFormat() {
        return hiveFormat;
    }

    public void setHiveFormat(String hiveFormat) {
        this.hiveFormat = hiveFormat;
    }
}


