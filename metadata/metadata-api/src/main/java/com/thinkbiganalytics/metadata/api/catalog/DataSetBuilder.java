/**
 * 
 */
package com.thinkbiganalytics.metadata.api.catalog;

import java.util.Map;

/**
 * Collections the details required to either create a new data set, or to identify
 * and existing data that meets the criteria specified by this builder.
 */
public interface DataSetBuilder {
    
    /**
     * @param title the title for the data set
     */
    DataSetBuilder title(String title);
    
    /**
     * @param description a description of the data set
     */
    DataSetBuilder description(String description);
    
    /**
     * @param format the format passed to the spark when the DataFrame is constructed.
     */
    DataSetBuilder format(String format);
    
    /**
     * Adds an spark option.
     * @param name the option name
     * @param value the option value
     */
    DataSetBuilder addOption(String name, String value);
    
    /**
     * Adds spark options.
     * @param options options to add
     */
    DataSetBuilder addOptions(Map<String, String> options);
    
    /**
     * @param path a path to add
     */
    DataSetBuilder addPath(String path);
    
    /**
     * @param paths paths to add
     */
    DataSetBuilder addPaths(Iterable<String> paths);
    
    /**
     * @param jarPath a jar path to add
     */
    DataSetBuilder addJar(String jarPath);
    
    /**
     * @param jarPaths jar paths to add
     */
    DataSetBuilder addJars(Iterable<String> jarPaths);
    
    /**
     * @param filePath a file path to add
     */
    DataSetBuilder addFile(String filePath);
    
    /**
     * @param filePaths file paths to add
     */
    DataSetBuilder addFiles(Iterable<String> filePaths);

    
    /**
     * If an existing data set exists effectively references the same underlying data
     * as indicated by the supplied values given this builder, then that data set will
     * be returned when this method is called.  If no data set exists yet that matches
     * these values then a new data set will be created.
     * @return a new or existing data set
     */
    DataSet build();
}
