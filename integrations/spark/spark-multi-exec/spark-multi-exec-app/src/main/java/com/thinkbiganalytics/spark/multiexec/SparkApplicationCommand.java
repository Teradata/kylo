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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 */
public class SparkApplicationCommand {
    
    private String name;
    private String className;
    private List<ApplicationArgument> args = new ArrayList<>();
    
    public SparkApplicationCommand() {
    }

    protected SparkApplicationCommand(String name, String className, Map<String, String> namedArgs, List<String> positionalArgs) {
        this.name = name;
        this.className = className;
        
        for (Entry<String, String> arg : namedArgs.entrySet()) {
            this.args.add(new ApplicationArgument(arg.getKey(), arg.getValue()));
        }
        
        for (String arg : positionalArgs) {
            this.args.add(new ApplicationArgument(arg));
        }
    }

    public String getName() {
        return this.name;
    }

    @JsonProperty("class")
    public String getClassName() {
        return this.className;
    }

    public List<ApplicationArgument> getArgs() {
        return this.args;
    }

    /**
     * @return an array of args suitable to pass to a main() method of the app.
     */
    public String[] asCommandLineArgs() {
        List<String> args = new ArrayList<>();
        
        for (ApplicationArgument arg : getArgs()) {
            if (arg.getName() != null) {
                args.add(asCommandSwitch(arg.getName()));
            }
            args.add(arg.getValue());
        }
        
        return args.toArray(new String[args.size()]);
    }

    private String asCommandSwitch(String name) {
        return name.length() == 1 ? "-" + name : "--" + name;
    }    

}
