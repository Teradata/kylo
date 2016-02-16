/**
 * 
 */
package com.thinkbiganalytics.nifi.processors.metadata;

import java.util.List;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;

/**
 *
 * @author Sean Felten
 */
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
}
