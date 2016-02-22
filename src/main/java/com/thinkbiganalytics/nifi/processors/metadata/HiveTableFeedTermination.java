/**
 * 
 */
package com.thinkbiganalytics.nifi.processors.metadata;

import java.util.List;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;

/**
 *
 * @author Sean Felten
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"feed", "termination", "thinkbig"})
@CapabilityDescription("Records the termination of a feed including the result of the process and table and partitions updated.")
public class HiveTableFeedTermination extends FeedTermination {

    @Override
    protected void addProperties(List<PropertyDescriptor> props) {
        super.addProperties(props);
        props.add(HiveTableProperties.DATABASE_NAME);
        props.add(HiveTableProperties.TABLE_NAME);
        props.add(HiveTableProperties.TABLE_LOCATION);
    }
    
    @Override
    protected Dataset createDestinationDataset(ProcessContext context, String datasetName, String descr) {
        DatasetProvider datasetProvider = getProviderService(context).getDatasetProvider();
        String databaseName = context.getProperty(HiveTableProperties.DATABASE_NAME).getValue();
        String tableName = context.getProperty(HiveTableProperties.TABLE_NAME).getValue();
        
        return datasetProvider.ensureHiveTableDataset(datasetName, "", databaseName, tableName);
    }
    
    @Override
    protected DataOperation completeOperation(ProcessContext context, 
                                              FlowFile flowFile, 
                                              Dataset dataset, 
                                              DataOperation op) {
        DataOperationsProvider opProvider = getProviderService(context).getDataOperationsProvider();
        HiveTableDataset hds = (HiveTableDataset) dataset;
        ChangeSet<HiveTableDataset, HiveTableUpdate> changeSet = opProvider.createChangeSet(hds, 0);
        
        return opProvider.updateOperation(op.getId(), "", changeSet);
    }
}
