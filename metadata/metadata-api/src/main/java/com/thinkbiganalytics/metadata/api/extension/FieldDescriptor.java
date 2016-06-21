/**
 * 
 */
package com.thinkbiganalytics.metadata.api.extension;

/**
 *
 * @author Sean Felten
 */
public interface FieldDescriptor {

    enum Type { STRING, BOOLEAN, INTEGER, LONG, DOUBLE, DATE, ENTITY } // TODO need BINARY? 


    Type getType();  // 
    
    String getName();
    
    String getDisplayName();
    
    String getDescription();
    
    boolean isCollection();
    
    boolean isRequired();
    
    // TODO do we need a default value.  If so then how do we represent it; especially for "entity" type
}
