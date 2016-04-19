package com.thinkbiganalytics.es;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 2/10/16.
 */
public class IndexMappingDTO {

    private String index;
    private List<TypeMappingDTO> types;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public List<TypeMappingDTO> getTypes() {
        if(types == null) {
            types = new ArrayList();
        }
        return types;
    }

    public void setTypes(List<TypeMappingDTO> types) {
        this.types = types;
    }

    public void addType(TypeMappingDTO type){
        getTypes().add(type);
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("index", index)
                .add("types", types)
                .toString();
    }
}
