/**
 * 
 */
package com.thinkbiganalytics.nifi.processors.metadata;

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
public class HiveTableFeedStart extends FeedStart {

    public static final String DATABASE_NAME_PROP = "database.name";
    public static final String TABLE_NAME_PROP = "table.name";
    public static final String TABLE_LOCATION_PROP = "table.location";
    public static final String TABLE_PARTITIONS_PROP = "table.partitions";
    
    public static final PropertyDescriptor DATABASE_NAME = new PropertyDescriptor.Builder()
            .name(DATABASE_NAME_PROP)
            .displayName("Database name")
            .description("The name of the database containing the table where the feed writes its processing results")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    public static final PropertyDescriptor TABLE_NAME = new PropertyDescriptor.Builder()
            .name(TABLE_NAME_PROP)
            .displayName("Table name")
            .description("The name of the table where the feed writes its processing results")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    public static final PropertyDescriptor TABLE_LOCATION = new PropertyDescriptor.Builder()
            .name(TABLE_LOCATION_PROP)
            .displayName("Table location")
            .description("The URI specifying the location where the feed writes its processing results")
            .required(false)
            .addValidator(StandardValidators.URI_VALIDATOR)
            .build();

    @Override
    protected void addProperties(List<PropertyDescriptor> props) {
        super.addProperties(props);
        props.add(DATABASE_NAME);
        props.add(TABLE_NAME);
        props.add(TABLE_LOCATION);
    }
    
    @Override
    protected Dataset createDestinationDataset(ProcessContext context, String datasetName, String descr) {
        DatasetProvider datasetProvider = getProviderService(context).getDatasetProvider();
        String databaseName = context.getProperty(DATABASE_NAME).getValue();
        String tableName = context.getProperty(TABLE_NAME).getValue();
        
        return datasetProvider.ensureHiveTableDataset(datasetName, "", databaseName, tableName);
    }
}
