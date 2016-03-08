/**
 * 
 */
package com.thinkbiganalytics.controller.precond;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.reporting.InitializationException;
import org.springframework.jms.annotation.JmsListener;

import com.thinkbiganalytics.controller.metadata.MetadataProviderService;
import com.thinkbiganalytics.metadata.event.jms.MetadataTopics;
import com.thinkbiganalytics.metadata.rest.model.event.DatasourceChangeEvent;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset;

/**
 *
 * @author Sean Felten
 */
public class JmsFeedPreconditionEventService extends AbstractControllerService implements FeedPreconditionEventService {


    public static final PropertyDescriptor METADATA_SERVICE = new PropertyDescriptor.Builder()
            .name("Metadata Provider Service")
            .description("Specified Service supplying the implemtentions of the various metadata providers")
            .required(true)
            .identifiesControllerService(MetadataProviderService.class)
            .build();
    
    private MetadataProviderService metadataService;
    private ConcurrentMap<String, Set<PreconditionListener>> listeners = new ConcurrentHashMap<>();
    
    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return Collections.singletonList(METADATA_SERVICE);
    }

    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {
        this.metadataService = context.getProperty(METADATA_SERVICE).asControllerService(MetadataProviderService.class);
        
        loadTestData();
    }

    private void loadTestData() {
        // TODO Auto-generated method stub
        
    }
    
    @JmsListener(destination = MetadataTopics.PRECONDITION_TRIGGER)
    public void receiveMetadataChange(DatasourceChangeEvent event) {
        for (Dataset ds : event.getDatasets()) {
            Set<PreconditionListener> listeners = this.listeners.get(ds.getDatasource().getName());
            
            if (listeners != null) {
                for (PreconditionListener listener : listeners) {
                    listener.triggered(event);
                }
            }
        }
        
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.precond.FeedPreconditionEventService#addListener(java.lang.String, com.thinkbiganalytics.controller.precond.PreconditionListener)
     */
    @Override
    public void addListener(String datasourceName, PreconditionListener listener) {
        // TODO Replace with selector filtering
        Set<PreconditionListener> set = this.listeners.get(datasourceName);
        if (set == null) {
            set = new HashSet<>();
            this.listeners.put(datasourceName, set);
        }
        set.add(listener);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.precond.FeedPreconditionEventService#removeListener(com.thinkbiganalytics.controller.precond.PreconditionListener)
     */
    @Override
    public void removeListener(PreconditionListener listener) {
        this.listeners.values().remove(listener);
    }
    

}
