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

import com.thinkbiganalytics.controller.metadata.MetadataProvider;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation.State;


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
    protected Datasource createDestinationDatasource(ProcessContext context, String datasetName, String descr) {
        MetadataProvider provider = getProviderService(context).getProvider();
        String path = context.getProperty(DirectoryProperties.DIRECTORY_PATH).getValue();
        
        return provider.ensureDirectoryDatasource(datasetName, "", Paths.get(path));
    }
    
    @Override
    protected DataOperation completeOperation(ProcessContext context, 
                                              FlowFile flowFile, 
                                              Datasource datasource, 
                                              DataOperation op,
                                              DataOperation.State state) {
        MetadataProvider provider = getProviderService(context).getProvider();
        DirectoryDatasource dds = (DirectoryDatasource) datasource;
        
        if (state == State.SUCCESS) {
            ArrayList<Path> paths = new ArrayList<>();
            // TODO Extract file paths from flow file
            Dataset dataset = provider.createDataset(dds, paths);
            
            return provider.completeOperation(op.getId(), "", dataset);
        } else {
            return provider.completeOperation(op.getId(), "", state);
        }
        
    }
}
