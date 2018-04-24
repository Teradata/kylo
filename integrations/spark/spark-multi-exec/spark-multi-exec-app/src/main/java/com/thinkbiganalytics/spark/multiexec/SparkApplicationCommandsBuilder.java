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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class SparkApplicationCommandsBuilder {
    
    private List<SparkApplicationCommand> specs = new ArrayList<>();

    public SparkCommandBuilder application(String name) {
        return new SparkCommandBuilder(this, name);
    }

    public List<SparkApplicationCommand> build() {
        return new ArrayList<>(this.specs);
    }
    
    protected void addSpec(SparkApplicationCommand spec) {
        this.specs.add(spec);
    }

    
    public static class SparkCommandBuilder {
        
        private SparkApplicationCommandsBuilder parent;
        private String name;
        private String className;
        private Map<String, String> namedArgs = new HashMap<>();
        private List<String> positionalArgs = new ArrayList<>();
        
        public SparkCommandBuilder(SparkApplicationCommandsBuilder parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        public SparkCommandBuilder className(String clsName) {
            this.className = clsName;
            return this;
        }

        public SparkCommandBuilder addArgument(String name, String value) {
            this.namedArgs.put(name, value);
            return this;
        }

        public SparkCommandBuilder addArgument(String value) {
            this.positionalArgs.add(value);
            return this;
        }

        public SparkApplicationCommandsBuilder add() {
            this.parent.addSpec(new SparkApplicationCommand(this.name, this.className, this.namedArgs, this.positionalArgs));
            return this.parent;
        }
        
    }
}
