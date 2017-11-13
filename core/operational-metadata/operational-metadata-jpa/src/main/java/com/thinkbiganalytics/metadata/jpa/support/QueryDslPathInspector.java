package com.thinkbiganalytics.metadata.jpa.support;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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

import com.querydsl.core.types.dsl.BeanPath;
import com.querydsl.core.types.dsl.EntityPathBase;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 */
public class QueryDslPathInspector {


    /**
     * returns a Map of the Field Name and the respective Field object for a given class
     */
    public static Map<String, Field> getFields(Class<?> cl) {
        return Arrays.asList(cl.getDeclaredFields()).stream().collect(Collectors.toMap(f -> f.getName(), f -> f));
    }


    /**
     * for a given path (separated by dot) get the final object
     *
     * @param basePath the object to start inspecting example:  QJpaBatchJobExecution jpaBatchJobExecution
     * @param fullPath a string representing the path you wish to inspect.  example:  jobInstance.jobName
     * @return return the Object for the path.  example: will return the StringPath jobName on the QJpaBatchJobInstance class
     */
    public static Object getFieldObject(BeanPath basePath, String fullPath) throws IllegalAccessException {

        LinkedList<String> paths = new LinkedList<>();
        paths.addAll(Arrays.asList(StringUtils.split(fullPath, ".")));
        return getFieldObject(basePath, paths);
    }


    private static Object getFieldObject(BeanPath basePath, LinkedList<String> paths) throws IllegalAccessException {
        if (!paths.isEmpty()) {
            String currPath = paths.pop();
            Object o = getObjectForField(basePath, currPath);
            if (o != null && o instanceof BeanPath && !paths.isEmpty()) {
                return getFieldObject((BeanPath) o, paths);
            }
            return o;
        }
        return null;
    }



    private static Object getObjectForField(BeanPath basePath, String field) throws IllegalAccessException {
        Map<String, Field> fieldSet = getFields(basePath.getClass());
        if (StringUtils.isNotBlank(field)) {
            field = StringUtils.trim(field);
            Field f = fieldSet.get(field);
            if (f != null) {
                Object o = f.get(basePath);
                return o;
            }
        }
        return null;

    }


}
