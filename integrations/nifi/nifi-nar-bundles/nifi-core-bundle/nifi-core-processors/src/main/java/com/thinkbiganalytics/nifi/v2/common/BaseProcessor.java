/**
 *
 */
package com.thinkbiganalytics.nifi.v2.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;

/**
 * @author Sean Felten
 */
public abstract class BaseProcessor extends AbstractProcessor {

    private Set<Relationship> relationships;
    private List<PropertyDescriptor> properties;

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractSessionFactoryProcessor#init(org.apache.nifi.processor.ProcessorInitializationContext)
     */
    @Override
    protected void init(final ProcessorInitializationContext context) {
        final Set<Relationship> relationships = new HashSet<>();
        addRelationships(relationships);
        this.relationships = Collections.unmodifiableSet(relationships);

        final List<PropertyDescriptor> properties = new ArrayList<>();
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
     */
    protected void addProperties(List<PropertyDescriptor> list) {
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
