/**
 *
 */
package com.thinkbiganalytics.nifi.v2.metadata;

import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.*;

/**
 * @author Sean Felten
 */
public abstract class AbstractFeedProcessor extends AbstractProcessor {

    public static final DateTimeFormatter TIME_FORMATTER = ISODateTimeFormat.dateTime();

    public static final String FEED_ID_PROP = "feed.id";
    public static final String SRC_DATASET_ID_PROP = "src.dataset.id";
    public static final String DEST_DATASET_ID_PROP = "dest.dataset.id";
    public static final String OPERATON_START_PROP = "operation.start.time";
    public static final String OPERATON_STOP_PROP = "operation.stop.time";

    public static final PropertyDescriptor METADATA_SERVICE = new PropertyDescriptor.Builder()
            .name("Metadata Provider Service")
            .description("Specified Service supplying the implemtentions of the various metadata providers")
            .required(true)
            .identifiesControllerService(MetadataProviderService.class)
            .build();


    private Set<Relationship> relationships;
    private List<PropertyDescriptor> properties;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final Set<Relationship> relationships = new HashSet<>();
        addRelationships(relationships);
        this.relationships = Collections.unmodifiableSet(relationships);

        final List<PropertyDescriptor> properties = new ArrayList<>();
        addProperties(properties);
        this.properties = Collections.unmodifiableList(properties);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    protected void addProperties(List<PropertyDescriptor> props) {
        props.add(METADATA_SERVICE);
    }

    protected void addRelationships(Set<Relationship> relationships2) {
    }

    protected MetadataProviderService getProviderService(ProcessContext context) {
        return context.getProperty(METADATA_SERVICE).asControllerService(MetadataProviderService.class);
    }

    protected Datasource findDatasource(ProcessContext context, String dsName) {
        MetadataProvider datasetProvider = getProviderService(context).getProvider();
        return datasetProvider.getDatasourceByName(dsName);
    }
}
