/**
 * 
 */
package com.thinkbiganalytics.controller.precond;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.jms.annotation.JmsListener;

import com.thinkbiganalytics.metadata.event.jms.MetadataTopics;
import com.thinkbiganalytics.metadata.rest.model.event.DatasourceChangeEvent;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset;

/**
 *
 * @author Sean Felten
 */
public class JmsPreconditionEventConsumer implements PreconditionEventConsumer {

    private ConcurrentMap<String, Set<PreconditionListener>> listeners = new ConcurrentHashMap<>();
    
//    @JmsListener(destination = MetadataTopics.PRECONDITION_TRIGGER, containerFactory="jmsContainerFactory" )
//    public void receiveMetadataChange(String event) {
//
//        System.out.println("MEssage: " + event);
//    }
    
    @JmsListener(destination = MetadataTopics.PRECONDITION_TRIGGER, containerFactory="metadataListenerContainerFactory")
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
    
    public void addListener(String datasourceName, PreconditionListener listener) {
        Set<PreconditionListener> set = this.listeners.get(datasourceName);
        if (set == null) {
            set = new HashSet<>();
            this.listeners.put(datasourceName, set);
//            Set<PreconditionListener> prev = this.listeners.putIfAbsent(datasourceName, set);
//            set = prev == null ? set : prev;
        }
        set.add(listener);
    }
    
    public void removeListener(PreconditionListener listener) {
        this.listeners.values().remove(listener);
    }

}
