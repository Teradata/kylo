package com.thinkbiganalytics.metadata.api.catalog;

import com.thinkbiganalytics.metadata.api.Propertied;
import com.thinkbiganalytics.metadata.api.SystemEntity;

import java.io.Serializable;
import java.util.List;

public interface Schema extends SystemEntity, Propertied {

    interface ID extends Serializable { }

    String getCharset();
    void setCharset(String name);

    List<? extends SchemaField> getFields();
    void setFields(List<? extends SchemaField> fields);
}
