/**
 * 
 */
package com.thinkbiganalytics.spark.multiexec;

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
