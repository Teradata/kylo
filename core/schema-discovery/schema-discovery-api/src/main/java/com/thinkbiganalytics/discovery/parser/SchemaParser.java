package com.thinkbiganalytics.discovery.parser;

/*-
 * #%L
 * thinkbig-schema-discovery-api
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SchemaParser {

    /**
     * Name of schema parser
     *
     * @return name
     */
    String name();

    /**
     * Description of parser
     *
     * @return description
     */
    String description();

    /**
     * Whether parser supports binary types
     *
     * @return true/false
     */
    boolean supportsBinary() default false;

    /**
     * Whether parser supports preview
     *
     * @return true/false
     */
    boolean supportsPreview() default true;

    /**
     * Whether parser allows skipping the header in file
     *
     * @return true/false
     */
    boolean allowSkipHeader() default false;

    /**
     * Whether parser generates a Hive Serde
     *
     * @return true/false
     */
    boolean generatesHiveSerde() default true;

    String[] tags() default "";

    /**
     * Returns a client-side helper for configuring the parser
     */
    String clientHelper() default "";
}
