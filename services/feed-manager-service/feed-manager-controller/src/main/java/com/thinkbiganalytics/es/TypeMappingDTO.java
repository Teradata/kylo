package com.thinkbiganalytics.es;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 2/10/16.
 */
public class TypeMappingDTO {
    String type;
    List<String> fields;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getFields() {
        if(fields == null) {
            fields = new ArrayList<>();
        }
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("fields", fields)
                .toString();
    }
}
