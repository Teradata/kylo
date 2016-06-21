/**
 * 
 */
package com.thinkbiganalytics.metadata.api.extension;

/**
 *
 * @author Sean Felten
 */
public interface ExtensibleTypeBuilder {
    
    ExtensibleTypeBuilder supertype(ExtensibleType type);
    
    ExtensibleTypeBuilder displayName(String dispName);
    
    ExtensibleTypeBuilder description(String descr);
    
    ExtensibleTypeBuilder addField(String name, FieldDescriptor.Type type);
    
    FieldDescriptorBuilder field(String name);

    ExtensibleType build();
}
