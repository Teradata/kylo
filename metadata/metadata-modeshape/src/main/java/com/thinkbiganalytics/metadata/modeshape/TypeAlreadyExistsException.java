/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

/**
 *
 * @author Sean Felten
 */
public class TypeAlreadyExistsException extends MetadataRepositoryException {
    
    private static final long serialVersionUID = 3674391008715729343L;
    
    private final String typeNmae;
    
    public TypeAlreadyExistsException(String typeName) {
        super("A type already exists with the name: " + typeName);
        this.typeNmae = typeName;
    }

    public String getTypeNmae() {
        return typeNmae;
    }
}
