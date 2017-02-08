package com.thinkbiganalytics.es;

/*-
 * #%L
 * thinkbig-feed-manager-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Return the Elastic Search Index Mapping data
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
        if (types == null) {
            types = new ArrayList();
        }
        return types;
    }

    public void setTypes(List<TypeMappingDTO> types) {
        this.types = types;
    }

    public void addType(TypeMappingDTO type) {
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
