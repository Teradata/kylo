/**
 * 
 */
package com.thinkbiganalytics.nifi.processors.metadata;

import java.nio.file.Paths;
import java.util.List;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;

/**
 *
 * @author Sean Felten
 */
public class DirectoryFeedTermination extends FeedTermination {

    @Override
    protected void addProperties(List<PropertyDescriptor> props) {
        super.addProperties(props);
        props.add(DirectoryProperties.DIRECTORY_PATH);
    }
    
    @Override
    protected Dataset createDestinationDataset(ProcessContext context, String datasetName, String descr) {
        DatasetProvider datasetProvider = getProviderService(context).getDatasetProvider();
        String path = context.getProperty(DirectoryProperties.DIRECTORY_PATH).getValue();
        
        return datasetProvider.ensureDirectoryDataset(datasetName, "", Paths.get(path));
    }
}
