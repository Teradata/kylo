package com.thinkbiganalytics.kylo.utils;

/*-
 * #%L
 * kylo-spark-livy-server
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.kylo.spark.livy.SparkLivyRestClient;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public class ScalaScriptUtils {
    private static final XLogger logger = XLoggerFactory.getXLogger(SparkLivyRestClient.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private ScalaScriptUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a scala string literal of the given object
     *
     * @return a literal that can be placed in a scala code snippet
     */
    public static String scalaStr(Object obj) {
        if (obj == null) {
            return "null";
        } else {
            return "\"\"\"" + String.valueOf(obj) + "\"\"\"";
        }
    }

    /**
     * Serializes the given object to JSON and encloses the result in triple quotes so it can be placed directly into a scala script.
     *
     * @param objToSerialize
     * @return
     */
    public static String toJsonInScalaString(Object objToSerialize) {
        String objAsJson = "{}";
        try {
            objAsJson = mapper.writeValueAsString(objToSerialize);
        } catch (JsonProcessingException e) {
            logger.error("Unable to serialize '{}' to string: {}", objToSerialize.getClass(), objToSerialize);
        }

        return ScalaScriptUtils.scalaStr(objAsJson);
    }
}

