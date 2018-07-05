/**
 * 
 */
package com.thinkbiganalytics.metadata.api.catalog;

import java.util.List;
import java.util.Map;

/**
 * The parameters passed to spark when a data frame representing the data set is created. 
 */
public interface DataSetSparkParameters {
    
    /**
     * @return the format
     */
    String getFormat();

    /**
     * @return files to be placed in the working directory of each executor
     */
    List<String> getFiles();

    /**
     * @return Jar files to include on the driver and executor classpaths
     */
    List<String> getJars();

    /**
     * @return File paths (and URIs) for input data
     */
    List<String> getPaths();
    
    /**
     * @return Input or output options for the data source
     */
    Map<String, String> getOptions();

    /**
     * Adds/replaces an option value.
     * @param name option name
     * @param value option value
     * @return true if the option was added or updated
     */
    boolean addOption(String name, String value);
    
    /**
     * Removes an option
     * @param name option name
     * @return the value that was assigned to the option, or null if not present
     */
    String removeOption(String name);
}
