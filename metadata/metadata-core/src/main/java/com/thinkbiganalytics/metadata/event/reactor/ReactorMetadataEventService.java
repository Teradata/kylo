/**
 * 
 */
package com.thinkbiganalytics.metadata.event.reactor;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.ResolvableType;

import com.thinkbiganalytics.metadata.api.event.EventMatcher;
import com.thinkbiganalytics.metadata.api.event.MetadataEvent;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;

import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;
import reactor.bus.selector.HeaderResolver;
import reactor.bus.selector.Selector;
import reactor.fn.Consumer;

/**
 *
 * @author Sean Felten
 */
public class ReactorMetadataEventService implements MetadataEventService {
    
    @Inject
    @Named("metadataEventBus")
    private EventBus eventBus;

    private final Map<MetadataEventListener<?>, Registration<?, ?>> registrations;
    
    /**
     * 
     */
    public ReactorMetadataEventService() {
        this.registrations = new ConcurrentHashMap<>();
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.event.MetadataEventService#notify(com.thinkbiganalytics.metadata.api.event.MetadataEvent)
     */
    @Override
    public <E extends MetadataEvent<? extends Serializable>> void notify(E event) {
        this.eventBus.notify(event, Event.wrap(event));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.event.MetadataEventService#addListener(com.thinkbiganalytics.metadata.api.event.MetadataEventListener)
     */
    @Override
    public <E extends MetadataEvent<? extends Serializable>> void addListener(MetadataEventListener<E> listener) {
        Registration<?,?> reg = this.eventBus.on(asSelector(listener), asConsumer(listener));
        this.registrations.put(listener, reg);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.event.MetadataEventService#addListener(com.thinkbiganalytics.metadata.api.event.MetadataEventListener, com.thinkbiganalytics.metadata.api.event.EventMatcher)
     */
    @Override
    public <E extends MetadataEvent<? extends Serializable>> void addListener(MetadataEventListener<E> listener, EventMatcher<E> matcher) {
        Registration<?,?> reg = this.eventBus.on(asSelector(matcher), asConsumer(listener));
        this.registrations.put(listener, reg);
    }

    @Override
    public void removeListener(MetadataEventListener<?> listener) {
        Registration<?, ?> reg = this.registrations.remove(listener);
        
        if (reg != null) {
            reg.cancel();
        }
    }

    private <E extends MetadataEvent<? extends Serializable>> Selector<E> asSelector(MetadataEventListener<E> listener) {
        return new EventTypeMatcher<>(listener);
    }
    
    private <E extends MetadataEvent<? extends Serializable>> Selector<E> asSelector(EventMatcher<E> matcher) {
        // TODO Auto-generated method stub
        return null;
    }

    private <E extends MetadataEvent<? extends Serializable>> Consumer<Event<E>> asConsumer(MetadataEventListener<E> listener) {
        return new ListenerConsumer<>(listener);
    }
    
    
    private static class ListenerConsumer<E extends MetadataEvent<? extends Serializable>> implements Consumer<Event<E>> {
        
        private final MetadataEventListener<E> listener;

        public ListenerConsumer(MetadataEventListener<E> listener) {
            super();
            this.listener = listener;
        }

        @Override
        public void accept(Event<E> event) {
            this.listener.notify(event.getData());
        }
    }

    
    
    private static class EventTypeMatcher<E extends MetadataEvent<? extends Serializable>> implements EventMatcher<E>, Selector<E> {
        
        private final Class<? extends MetadataEvent<?>> eventClass;
        private final Class<? extends Serializable> dataClass;
        
        public EventTypeMatcher(MetadataEventListener<E> listener) {
            ResolvableType listenerType = ResolvableType.forClass(MetadataEventListener.class, listener.getClass());
            
            @SuppressWarnings("unchecked")
            Class<? extends MetadataEvent<?>> evClass = (Class<? extends MetadataEvent<?>>) listenerType.resolveGeneric(0);
            
            ResolvableType evType = ResolvableType.forClass(MetadataEvent.class, evClass);
            
            @SuppressWarnings("unchecked")
            Class<? extends Serializable> serClass = (Class<? extends Serializable>) evType.resolveGeneric(0);
            
            this.eventClass = evClass;
            this.dataClass = serClass;
        }
        
        @Override
        public boolean test(E event) {
            if (this.eventClass.isAssignableFrom(event.getClass())) {
                return this.dataClass.isAssignableFrom(event.getData().getClass());
            } else {
                return false;
            }
        }
        
        @Override
        public boolean matches(E event) {
            return test(event);
        }

        @Override
        public Object getObject() {
            return null;
        }

        @Override
        public HeaderResolver<?> getHeaderResolver() {
            return null;
        }
    }

}
