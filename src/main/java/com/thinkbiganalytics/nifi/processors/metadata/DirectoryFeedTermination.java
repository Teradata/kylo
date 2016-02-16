/**
 * 
 */
package com.thinkbiganalytics.nifi.processors.metadata;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;

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
    
    @Override
    protected DataOperation completeOperation(ProcessContext context, 
                                              FlowFile flowFile, 
                                              Dataset dataset, 
                                              DataOperation op) {
        DataOperationsProvider opProvider = getProviderService(context).getDataOperationsProvider();
        DirectoryDataset dds = (DirectoryDataset) dataset;
        ArrayList<Path> paths = new ArrayList<>();
        // TODO Extract file paths from flow file
        ChangeSet<DirectoryDataset, FileList> changeSet = opProvider.createChangeSet(dds, paths);
        
        return opProvider.updateOperation(op.getId(), "", changeSet);
    }
}
