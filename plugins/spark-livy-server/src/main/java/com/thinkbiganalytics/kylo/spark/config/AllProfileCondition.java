package com.thinkbiganalytics.kylo.spark.config;

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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;

public class AllProfileCondition implements Condition {
    private static final Logger logger = LoggerFactory.getLogger(AllProfileCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (context.getEnvironment() != null) {
            MultiValueMap<String, Object> attrs = metadata.getAllAnnotationAttributes(AllProfiles.class.getName());
            if (attrs != null) {
                for (Object values : attrs.get("value")) {
                    if( !(values instanceof String[]) ) return false;
                    String[] vals = (String[]) values;
                    for( String value : vals ) {
                        if (!context.getEnvironment().acceptsProfiles(value))  {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return true;
    }
}
