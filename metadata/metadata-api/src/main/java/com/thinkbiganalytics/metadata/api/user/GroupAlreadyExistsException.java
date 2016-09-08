/**
 * 
 */
package com.thinkbiganalytics.metadata.api.user;

import com.thinkbiganalytics.metadata.api.MetadataException;

/**
 * Thrown when there is an attempt to create a group when one already exists with the same name.
 * 
 * @author Sean Felten
 */
public class GroupAlreadyExistsException extends MetadataException {

    private static final long serialVersionUID = 1L;
    
    private final String groupName;

    /**
     * @param groupName
     */
    public GroupAlreadyExistsException(String name) {
        super("A group with the name \"" + name + "\" already exists");
        this.groupName = name;
    }

    /**
     * @return the groupName
     */
    public String getGroupName() {
        return groupName;
    }
}
