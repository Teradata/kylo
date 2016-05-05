/**
 *
 */
package com.thinkbiganalytics.nifi.v2.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;

import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;

/**
 * @author Sean Felten
 */
public abstract class AbstractFeedProcessor extends AbstractProcessor {

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
