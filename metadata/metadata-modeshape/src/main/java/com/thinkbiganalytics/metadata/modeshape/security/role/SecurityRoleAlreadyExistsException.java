/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.role;

import com.thinkbiganalytics.metadata.api.MetadataException;

/**
 *
 */
public class SecurityRoleAlreadyExistsException extends MetadataException {

    private static final long serialVersionUID = 1L;
    
    private final String roleName;
    private final String entityName;
    
    public SecurityRoleAlreadyExistsException(String entityName, String roleName) {
        super("A role for the entity \"" + entityName + "\" already exists with the name: \"" + roleName + "\"");
        this.entityName = entityName;
        this.roleName = roleName;
    }
    
    /**
     * @return the entityName
     */
    public String getEntityName() {
        return entityName;
    }
    
    /**
     * @return the roleName
     */
    public String getRoleName() {
        return roleName;
    }
}
