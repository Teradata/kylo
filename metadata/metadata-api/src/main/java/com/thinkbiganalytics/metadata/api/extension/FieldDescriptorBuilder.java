/**
 * 
 */
package com.thinkbiganalytics.metadata.api.extension;

/**
 *
 * @author Sean Felten
 */
public interface FieldDescriptorBuilder {

    FieldDescriptorBuilder name(String name);
    
    FieldDescriptorBuilder type(FieldDescriptor.Type type);
    
    FieldDescriptorBuilder displayName(String name);
    
    FieldDescriptorBuilder description(String descr);
    
    FieldDescriptorBuilder collection();
    
    FieldDescriptorBuilder required();
    
    ExtensibleTypeBuilder add();
}
