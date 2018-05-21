package com.thinkbiganalytics.discovery.rest.controller;
/*-
 * #%L
 * thinkbig-schema-discovery-controller
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

import com.thinkbiganalytics.discovery.model.SchemaParserDescriptor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SchemaParserDescriptorUtil {


    public  static Comparator<SchemaParserDescriptor> compareByNameThenPrimaryThenSpark() {
        return Comparator.comparing(SchemaParserDescriptor::getName)
            .thenComparing((d1,d2) -> new Boolean(d2.isPrimary()).compareTo(new Boolean(d1.isPrimary())))
            .thenComparing((d1,d2) -> new Boolean(d2.isUsesSpark()).compareTo(new Boolean(d1.isUsesSpark())));
    }

    public static Comparator<SchemaParserDescriptor> compareByNameThenSpark() {
        return Comparator.comparing(SchemaParserDescriptor::getName)
            .thenComparing((d1,d2) -> new Boolean(d2.isUsesSpark()).compareTo(new Boolean(d1.isUsesSpark())));
    }

    /**
     * filters the incoming <code>list</code> keeping the first descriptor in the list their are duplicates based on the name
     * @param list list of schema descriptors
     * @return the filtered list removing duplicates based upon name
     */
    public static List<SchemaParserDescriptor> keepFirstByName(List<SchemaParserDescriptor> list){
        List<SchemaParserDescriptor> descriptors = new ArrayList<>();
        Set<String> names = new HashSet<>();
        list.stream().forEach(descriptor -> {
            if(!names.contains(descriptor.getName())){
                descriptors.add(descriptor);
                names.add(descriptor.getName());
            }
        });
        return descriptors;
    }

}
