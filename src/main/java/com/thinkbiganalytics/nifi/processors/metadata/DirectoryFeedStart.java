/**
 * 
 */
package com.thinkbiganalytics.nifi.processors.metadata;

import java.nio.file.Paths;
import java.util.List;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.util.StandardValidators;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;

/**
 *
 * @author Sean Felten
 */
public class DirectoryFeedStart extends FeedStart {

    public static final String DIRECTORY_PATH_PROP = "directory.path";
    
    public static final PropertyDescriptor DIRECTORY_PATH = new PropertyDescriptor.Builder()
            .name(DIRECTORY_PATH_PROP)
            .displayName("Directory path")
            .description("The directory path where the feed puts it processing results")
            .required(true)
            .addValidator(StandardValidators.createDirectoryExistsValidator(false, true))
            .build();

    @Override
    protected void addProperties(List<PropertyDescriptor> props) {
        super.addProperties(props);
        props.add(DIRECTORY_PATH);
    }
    
    @Override
    protected Dataset createDestinationDataset(ProcessContext context, String datasetName, String descr) {
        DatasetProvider datasetProvider = getProviderService(context).getDatasetProvider();
        String path = context.getProperty(DIRECTORY_PATH).getValue();
        
        return datasetProvider.ensureDirectoryDataset(datasetName, "", Paths.get(path));
    }
}
