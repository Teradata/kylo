/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.schema;

import java.util.List;


public interface Field {

    String getName();

    String getDescription();

    String getNativeDataType();

    String getDerivedDataType();

    void setDerivedDataType(String type);

    Boolean isPrimaryKey();

    Boolean isNullable();

    List<String> getSampleValues();

}


