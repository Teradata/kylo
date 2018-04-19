/**
 *
 */
package com.thinkbiganalytics.nifi.processor;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 */
public abstract class BaseProcessor extends AbstractNiFiProcessor {

    private Set<Relationship> relationships;
    private List<PropertyDescriptor> properties;

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractSessionFactoryProcessor#init(org.apache.nifi.processor.ProcessorInitializationContext)
     */
    @Override
    protected void init(final ProcessorInitializationContext context) {
        super.init(context);

        final Set<Relationship> relationships = new HashSet<>();
        addRelationships(relationships);
        this.relationships = Collections.unmodifiableSet(relationships);
        
        final LinkedHashSet<PropertyDescriptor> propSet = new LinkedHashSet<>();
        addProperties(propSet);

        final List<PropertyDescriptor> properties = new ArrayList<>(propSet);
        addProperties(properties);
        this.properties = Collections.unmodifiableList(properties);
    }

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractSessionFactoryProcessor#getRelationships()
     */
    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    /* (non-Javadoc)
     * @see org.apache.nifi.components.AbstractConfigurableComponent#getSupportedPropertyDescriptors()
     */
    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    /**
     * Convenience method for collecting the properties that the processor will support.
     * Subclasses should call super.addProperties(list) before adding additional properties.
     *
     * @param props the list to which is added this processor's properties
     * @deprecated Use addProperties(Set) instead for greater flexibility
     */
    @Deprecated
    protected void addProperties(List<PropertyDescriptor> list) {
    }
    
    /**
     * Convenience method for collecting the properties that the processor will support.  The
     * supplied set is ordered and modifiable.  Subclasses should add their properties before
     * calling super.addProperties(set) to supersede the superclass' properties, or after the
     * call to add/modify the superclass property set. 
     *
     * @param props the set to which is added this processor's properties
     */
    protected void addProperties(Set<PropertyDescriptor> orderedSet) {
    }

    /**
     * Convenience method for collecting the relationships that the processor will support.
     * Subclasses should call super.addRelationship(set) before adding additional relationships.
     *
     * @param props the set to which is added this processor's relationships
     */
    protected void addRelationships(Set<Relationship> set) {
    }
}
