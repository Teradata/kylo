/**
 * 
 */
package com.thinkbiganalytics.metadata.api.user;

import com.thinkbiganalytics.metadata.api.MetadataException;

/**
 * Thrown when there is an attempt to create a user when one already exists with the same name.
 * 
 * @author Sean Felten
 */
public class UserAlreadyExistsException extends MetadataException {

    private static final long serialVersionUID = 1L;
    
    private final String username;

    /**
     * @param username
     */
    public UserAlreadyExistsException(String username) {
        super("A user with the name \"" + username + "\" already exists");
        this.username = username;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }
}
