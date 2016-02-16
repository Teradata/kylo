/**
 * 
 */
package com.thinkbiganalytics.nifi.processors.metadata;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.util.StandardValidators;

/**
 *
 * @author Sean Felten
 */
public interface DirectoryProperties {

    public static final String DIRECTORY_PATH_PROP = "directory.path";
    
    public static final PropertyDescriptor DIRECTORY_PATH = new PropertyDescriptor.Builder()
            .name(DIRECTORY_PATH_PROP)
            .displayName("Directory path")
            .description("The path for the directory dataset")
            .required(true)
            .addValidator(StandardValidators.createDirectoryExistsValidator(false, true))
            .build();

}
