package com.thinkbiganalytics.metadata.api.catalog;

import com.thinkbiganalytics.metadata.api.Taggable;

import java.io.Serializable;

public interface SchemaField extends Taggable {

    interface ID extends Serializable { }

    void setDatatype(String datatype);
    String getDatatype();

    void setDescription(String description);
    String getDescription();

    void setName(String name);
    String getName();

    void setSystemName(String systemName);
    String getSystemName();
}
