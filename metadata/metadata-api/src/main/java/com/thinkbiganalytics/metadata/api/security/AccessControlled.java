/**
 * 
 */
package com.thinkbiganalytics.metadata.api.security;

import com.thinkbiganalytics.security.action.AllowedActions;

/**
 * Defines the ability to access an entity.  Top-level entities may implement this interface to advertise 
 * the possible permission that may be applied to it and to apply those permissions.
 * @author Sean Felten
 */
public interface AccessControlled {

    AllowedActions getAllowedActions();
}
