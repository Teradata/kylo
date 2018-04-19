/**
 * 
 */
package com.thinkbiganalytics.spark.multiexec;

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
