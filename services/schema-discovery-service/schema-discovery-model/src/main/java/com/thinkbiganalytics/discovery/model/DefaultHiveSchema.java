package com.thinkbiganalytics.discovery.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.discovery.schema.HiveTableSchema;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultHiveSchema extends DefaultTableSchema implements HiveTableSchema {

    private String hiveFormat;

    private Boolean structured = false;

    public String getHiveFormat() {
        return hiveFormat;
    }

    public void setHiveFormat(String hiveFormat) {
        this.hiveFormat = hiveFormat;
    }

    @Override
    public Boolean isStructured() {
        return structured;
    }

    @Override
    public void setStructured(boolean structured) {
        this.structured = true;
    }
}


