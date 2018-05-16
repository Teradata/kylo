/**
 * 
 */
package com.thinkbiganalytics.spark.multiexec;

/*-
 * #%L
 * kylo-spark-multi-exec-app
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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class ApplicationArgument {
    
    private String name;
    private String value;
    
    public ApplicationArgument() {
    }
    
    public ApplicationArgument(String value) {
        this(null, value);
    }

    public ApplicationArgument(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }

    @JsonProperty("a")  // Reduces command line size
    public String getName() {
        return this.name;
    }

    @JsonProperty("v")  // Reduces command line size
    public String getValue() {
        return this.value;
    }
}
